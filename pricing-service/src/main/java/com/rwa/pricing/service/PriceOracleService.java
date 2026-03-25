package com.rwa.pricing.service;

import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.AssetPriceUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import java.util.Random;

@Service
@EnableCaching
@Slf4j
@RequiredArgsConstructor
public class PriceOracleService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final Random random = new Random();

    // Mocking an asset ID for demonstration
    public static final UUID GOLD_ASSET_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

    @Scheduled(fixedRate = 30000) // Every 30 seconds
    @CachePut(value = "asset-prices", key = "T(com.rwa.pricing.service.PriceOracleService).GOLD_ASSET_ID")
    public BigDecimal updatePrices() {
        BigDecimal currentPrice = BigDecimal.valueOf(2000 + (random.nextDouble() * 100)); // Simulating price fluctuation
        currentPrice = currentPrice.setScale(2, RoundingMode.HALF_UP);

        AssetPriceUpdatedEvent event = AssetPriceUpdatedEvent.builder()
                .assetId(GOLD_ASSET_ID)
                .newPrice(currentPrice)
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Broadcasting price update for asset {}: ${}", GOLD_ASSET_ID, currentPrice);
        kafkaTemplate.send(RwaConstants.TOPIC_PRICE_EVENTS, GOLD_ASSET_ID.toString(), event);
        return currentPrice;
    }
}
