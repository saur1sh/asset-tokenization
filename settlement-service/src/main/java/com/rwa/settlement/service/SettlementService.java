package com.rwa.settlement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwa.common.events.FundsLockedEvent;
import com.rwa.common.events.InventoryReservedEvent;
import com.rwa.common.events.TradeFailedEvent;
import com.rwa.common.events.TradeInitiatedEvent;
import com.rwa.common.events.TradeMatchedEvent;
import com.rwa.settlement.entity.OutboxEvent;
import com.rwa.settlement.entity.TradeSaga;
import com.rwa.settlement.repository.OutboxEventRepository;
import com.rwa.settlement.repository.TradeSagaRepository;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.enums.SagaStatus;
import com.rwa.common.enums.TradeEventStatus;
import com.rwa.common.exception.ResourceNotFoundException;
import com.rwa.common.exception.RwaBaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettlementService {

    private final TradeSagaRepository sagaRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public TradeSaga initiateTrade(UUID buyerId, UUID assetId, BigDecimal fractions, BigDecimal priceAmount) {
        TradeSaga saga = TradeSaga.builder()
                .id(UUID.randomUUID())
                .buyerId(buyerId)
                .assetId(assetId)
                .fractions(fractions)
                .priceAmount(priceAmount)
                .status(SagaStatus.PENDING.name())
                .fundsLocked(false)
                .inventoryReserved(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        sagaRepository.save(saga);
        log.info("Initiated Trade Saga {}", saga.getId());

        TradeInitiatedEvent event = TradeInitiatedEvent.builder()
                .tradeId(saga.getId())
                .buyerId(buyerId)
                .assetId(assetId)
                .fractions(fractions)
                .priceAmount(priceAmount)
                .build();
                
        saveToOutbox(RwaConstants.TOPIC_TRADE_INITIATED, event, saga.getId());
        return saga;
    }

    @Transactional
    public void handleFundsLocked(FundsLockedEvent event) {
        log.info("Handling FundsLockedEvent for trade {} with status {}", event.getTradeId(), event.getStatus());
        TradeSaga saga = sagaRepository.findById(event.getTradeId())
                .orElseThrow(() -> new ResourceNotFoundException("TradeSaga", event.getTradeId().toString()));
        
        if (TradeEventStatus.LOCKED.name().equals(event.getStatus())) {
            saga.setFundsLocked(true);
            checkCompletion(saga);
        } else {
            saga.setStatus(SagaStatus.FAILED.name());
            log.warn("Trade {} failed due to insufficient funds.", saga.getId());
            emitTradeFailedEvent(saga, "FUNDS_LOCK_FAILED");
        }
        
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepository.save(saga);
    }

    @Transactional
    public void handleInventoryReserved(InventoryReservedEvent event) {
        log.info("Handling InventoryReservedEvent for trade {} with status {}", event.getTradeId(), event.getStatus());
        TradeSaga saga = sagaRepository.findById(event.getTradeId())
                .orElseThrow(() -> new ResourceNotFoundException("TradeSaga", event.getTradeId().toString()));
        
        if (TradeEventStatus.RESERVED.name().equals(event.getStatus())) {
            saga.setInventoryReserved(true);
            checkCompletion(saga);
        } else {
            saga.setStatus(SagaStatus.FAILED.name());
            log.warn("Trade {} failed due to lack of inventory.", saga.getId());
            emitTradeFailedEvent(saga, "INVENTORY_RESERVE_FAILED");
        }
        
        saga.setUpdatedAt(LocalDateTime.now());
        sagaRepository.save(saga);
    }

    private void checkCompletion(TradeSaga saga) {
        if (saga.isFundsLocked() && saga.isInventoryReserved() && SagaStatus.PENDING.name().equals(saga.getStatus())) {
            saga.setStatus(SagaStatus.COMPLETED.name());
            log.info("Trade Saga {} is COMPLETED successfully. Triggering on-chain settlement.", saga.getId());

            TradeMatchedEvent matchedEvent = TradeMatchedEvent.builder()
                    .tradeId(saga.getId())
                    .buyerId(saga.getBuyerId())
                    .sellerId(UUID.fromString("00000000-0000-0000-0000-000000000000")) // Platform/Issuer
                    .assetId(saga.getAssetId())
                    .quantity(saga.getFractions())
                    .price(saga.getPriceAmount())
                    .timestamp(System.currentTimeMillis())
                    .build();

            saveToOutbox(RwaConstants.TOPIC_TRADE_MATCHES, matchedEvent, saga.getId());
        }
    }

    private void emitTradeFailedEvent(TradeSaga saga, String reason) {
        TradeFailedEvent event = TradeFailedEvent.builder()
                .tradeId(saga.getId())
                .buyerId(saga.getBuyerId())
                .assetId(saga.getAssetId())
                .fractions(saga.getFractions())
                .priceAmount(saga.getPriceAmount())
                .reason(reason)
                .build();
        saveToOutbox(RwaConstants.TOPIC_TRADE_FAILED, event, saga.getId());
    }

    public TradeSaga getTradeSaga(UUID id) {
        return sagaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("TradeSaga", id.toString()));
    }

    private void saveToOutbox(String topic, Object eventPayload, UUID aggregateId) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(RwaConstants.AGGREGATE_TRADE)
                    .aggregateId(aggregateId.toString())
                    .type(topic)
                    .payload(objectMapper.writeValueAsString(eventPayload))
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
            throw new RwaBaseException("Serialization failure", e);
        }
    }
}
