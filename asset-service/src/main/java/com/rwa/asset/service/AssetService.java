package com.rwa.asset.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwa.asset.entity.AssetReservation;
import com.rwa.asset.entity.DigitalAsset;
import com.rwa.asset.entity.OutboxEvent;
import com.rwa.asset.repository.AssetRepository;
import com.rwa.asset.repository.AssetReservationRepository;
import com.rwa.asset.repository.OutboxEventRepository;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.enums.TradeEventStatus;
import com.rwa.common.events.InventoryReservedEvent;
import com.rwa.common.events.TradeInitiatedEvent;
import com.rwa.common.exception.ResourceNotFoundException;
import com.rwa.common.exception.RwaBaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository repository;
    private final AssetReservationRepository reservationRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "assets", key = "#id")
    public DigitalAsset getAsset(UUID id) {
        log.info("Fetching asset {} from database", id);
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", id.toString()));
    }

    @Transactional
    @CacheEvict(value = "assets", key = "#result.id", condition = "#result != null")
    public DigitalAsset createAsset(String type, BigDecimal totalFractions) {
        DigitalAsset asset = DigitalAsset.builder()
                .id(UUID.randomUUID())
                .type(type)
                .totalFractions(totalFractions)
                .remainingFractions(totalFractions)
                .build();
        log.info("Creating new asset {} with {} fractions", asset.getId(), totalFractions);
        return repository.save(asset);
    }

    @Transactional
    @CacheEvict(value = "assets", key = "#event.assetId")
    public void reserveTokens(TradeInitiatedEvent event) {
        log.info("Attempting to reserve {} fractions for asset {} (Trade: {})", 
                event.getFractions(), event.getAssetId(), event.getTradeId());

        if (reservationRepository.existsById(event.getTradeId())) {
            log.info("Trade {} already processed. Skipping.", event.getTradeId());
            return;
        }
                
        DigitalAsset asset = repository.findById(event.getAssetId()).orElse(null);
        TradeEventStatus status = TradeEventStatus.FAILED;
        
        if (asset != null) {
            if (asset.getRemainingFractions().compareTo(event.getFractions()) >= 0) {
                asset.setRemainingFractions(asset.getRemainingFractions().subtract(event.getFractions()));
                repository.save(asset); // Optimistic locking via @Version
                status = TradeEventStatus.RESERVED;
                
                AssetReservation reservation = AssetReservation.builder()
                        .tradeId(event.getTradeId())
                        .assetId(event.getAssetId())
                        .fractions(event.getFractions())
                        .status("RESERVED")
                        .createdAt(LocalDateTime.now())
                        .build();
                reservationRepository.save(reservation);
                log.info("Reserved fractions successfully.");
            } else {
                log.warn("Insufficient fractions available.");
            }
        } else {
            log.warn("Asset not found: {}", event.getAssetId());
        }

        InventoryReservedEvent reservedEvent = InventoryReservedEvent.builder()
                .tradeId(event.getTradeId())
                .assetId(event.getAssetId())
                .fractionsReserved(status == TradeEventStatus.RESERVED ? event.getFractions() : BigDecimal.ZERO)
                .status(status.name())
                .build();

        saveToOutbox(reservedEvent);
    }

    @Transactional
    @CacheEvict(value = "assets", key = "#assetId")
    public void updateAssetPrice(UUID assetId, BigDecimal newPrice) {
        repository.findById(assetId).ifPresentOrElse(asset -> {
            asset.setPrice(newPrice);
            repository.save(asset);
            log.info("Updated price for asset {} to ${}", assetId, newPrice);
        }, () -> log.warn("Asset {} not found for price update", assetId));
    }

    @Transactional
    @CacheEvict(value = "assets", key = "#assetId")
    public void releaseReservedFractions(UUID assetId, BigDecimal fractions, UUID tradeId) {
        reservationRepository.findById(tradeId).ifPresentOrElse(reservation -> {
            if ("RESERVED".equals(reservation.getStatus())) {
                DigitalAsset asset = repository.findById(assetId)
                        .orElseThrow(() -> new ResourceNotFoundException("Asset", assetId.toString()));
                asset.setRemainingFractions(asset.getRemainingFractions().add(reservation.getFractions()));
                repository.save(asset);
                
                reservation.setStatus("CANCELLED");
                reservationRepository.save(reservation);
                log.info("Released {} fractions for asset {} due to failed trade {}.", reservation.getFractions(), assetId, tradeId);
            } else {
                log.info("Reservation for trade {} is already {}", tradeId, reservation.getStatus());
            }
        }, () -> log.info("No reservation found for trade {}. Nothing to release.", tradeId));
    }

    private void saveToOutbox(InventoryReservedEvent event) {
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(RwaConstants.AGGREGATE_TRADE)
                    .aggregateId(event.getTradeId().toString())
                    .type(RwaConstants.TOPIC_INVENTORY_EVENTS)
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("Saved outbox event for Trade {}", event.getTradeId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize InventoryReservedEvent", e);
            throw new RwaBaseException("Serialization failure", e);
        }
    }
}
