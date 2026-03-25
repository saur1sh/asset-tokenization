package com.rwa.wallet.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.enums.ComplianceStatus;
import com.rwa.common.enums.TradeEventStatus;
import com.rwa.common.events.FundsLockedEvent;
import com.rwa.common.events.TradeInitiatedEvent;
import com.rwa.common.exception.ResourceNotFoundException;
import com.rwa.common.exception.RwaBaseException;
import com.rwa.wallet.entity.OutboxEvent;
import com.rwa.wallet.entity.Wallet;
import com.rwa.wallet.entity.WalletLock;
import com.rwa.wallet.repository.OutboxEventRepository;
import com.rwa.wallet.repository.WalletLockRepository;
import com.rwa.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletLockRepository lockRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${rwa.aml.limit:1000000}")
    private BigDecimal amlLimit;

    @Cacheable(value = "wallets", key = "#userId")
    public Wallet getWallet(UUID userId) {
        log.info("Fetching wallet for user {} from database", userId);
        return walletRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId.toString()));
    }

    @Transactional
    @CacheEvict(value = "wallets", key = "#userId")
    public Wallet createWallet(UUID userId, BigDecimal initialBalance, String currency) {
        Wallet wallet = Wallet.builder()
                .userId(userId)
                .balance(initialBalance)
                .currency(currency)
                .kycStatus(ComplianceStatus.UNVERIFIED.name())
                .build();
        log.info("Creating new wallet for user {} with balance {} (Status: UNVERIFIED)", userId, initialBalance);
        return walletRepository.save(wallet);
    }

    @Transactional
    @CacheEvict(value = "wallets", key = "#userId")
    public void updateKycStatus(UUID userId, ComplianceStatus status) {
        walletRepository.findById(userId).ifPresentOrElse(wallet -> {
            wallet.setKycStatus(status.name());
            walletRepository.save(wallet);
            log.info("Updated KYC status for user {} to {}", userId, status);
        }, () -> log.warn("Wallet not found for user: {}", userId));
    }

    @Transactional
    @CacheEvict(value = "wallets", key = "#event.buyerId")
    public void lockFunds(TradeInitiatedEvent event) {
        log.info("Attempting to lock funds {} for user {} (Trade: {})", 
                event.getPriceAmount(), event.getBuyerId(), event.getTradeId());

        if (lockRepository.existsById(event.getTradeId())) {
            log.info("Trade {} already processed. Skipping.", event.getTradeId());
            return;
        }
                
        Wallet wallet = walletRepository.findById(event.getBuyerId()).orElse(null);
        TradeEventStatus status = TradeEventStatus.FAILED;
        
        if (wallet != null) {
            if (!ComplianceStatus.VERIFIED.name().equals(wallet.getKycStatus())) {
                log.warn("Blocked: User {} is not KYC VERIFIED", event.getBuyerId());
            } else if (event.getPriceAmount().compareTo(amlLimit) > 0) {
                log.warn("Blocked: AML transaction limit exceeded for user {}", event.getBuyerId());
            } else if (wallet.getBalance().compareTo(event.getPriceAmount()) >= 0) {
                wallet.setBalance(wallet.getBalance().subtract(event.getPriceAmount()));
                walletRepository.save(wallet); // Optimistic locking via @Version
                status = TradeEventStatus.LOCKED;
                
                WalletLock lock = WalletLock.builder()
                        .tradeId(event.getTradeId())
                        .userId(event.getBuyerId())
                        .amount(event.getPriceAmount())
                        .status("LOCKED")
                        .createdAt(LocalDateTime.now())
                        .build();
                lockRepository.save(lock);
                log.info("Funds locked successfully.");
            } else {
                log.warn("Insufficient funds available.");
            }
        } else {
            log.warn("Wallet not found for user: {}", event.getBuyerId());
        }

        FundsLockedEvent lockedEvent = FundsLockedEvent.builder()
                .tradeId(event.getTradeId())
                .userId(event.getBuyerId())
                .amount(status == TradeEventStatus.LOCKED ? event.getPriceAmount() : BigDecimal.ZERO)
                .status(status.name())
                .build();

        saveToOutbox(lockedEvent);
    }

    @Transactional
    @CacheEvict(value = "wallets", key = "#userId")
    public void creditYield(UUID userId, BigDecimal amount) {
        walletRepository.findById(userId).ifPresentOrElse(wallet -> {
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);
            log.info("Credited yield of {} to user {}", amount, userId);
        }, () -> log.warn("Wallet not found for yield credit for user: {}", userId));
    }

    private void saveToOutbox(FundsLockedEvent event) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(RwaConstants.AGGREGATE_WALLET)
                    .aggregateId(event.getUserId().toString())
                    .type(RwaConstants.TOPIC_WALLET_EVENTS)
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event for Trade {}", event.getTradeId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize FundsLockedEvent", e);
            throw new RwaBaseException("Serialization failure", e);
        }
    }

    @Transactional
    @CacheEvict(value = "wallets", key = "#userId")
    public void refundFunds(UUID userId, BigDecimal amount, UUID tradeId) {
        lockRepository.findById(tradeId).ifPresentOrElse(lock -> {
            if ("LOCKED".equals(lock.getStatus())) {
                Wallet wallet = walletRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Wallet", userId.toString()));
                wallet.setBalance(wallet.getBalance().add(lock.getAmount()));
                walletRepository.save(wallet);
                
                lock.setStatus("RELEASED");
                lockRepository.save(lock);
                log.info("Refunded {} to user {} for failed trade {}.", lock.getAmount(), userId, tradeId);
            } else {
                log.info("Lock for trade {} is already {}", tradeId, lock.getStatus());
            }
        }, () -> log.info("No lock found for trade {}. Nothing to refund.", tradeId));
    }
}
