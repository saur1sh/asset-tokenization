package com.rwa.asset.service;

import com.rwa.common.events.YieldDistributedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class YieldDistributionService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Mocked gold asset same as in pricing service
    private static final UUID GOLD_ASSET_ID = UUID.fromString("f47ac10b-58cc-4372-a567-0e02b2c3d479");

    @Scheduled(cron = "0 0 0 * * ?") // Daily at midnight
    public void distributeDailyYields() {
        BigDecimal totalYield = new BigDecimal("500.00"); // Simulated total yield for the day
        BigDecimal yieldPerFraction = new BigDecimal("0.50"); // 500 / 1000 total fractions

        YieldDistributedEvent event = YieldDistributedEvent.builder()
                .assetId(GOLD_ASSET_ID)
                .totalYieldAmount(totalYield)
                .amountPerFraction(yieldPerFraction)
                .timestamp(System.currentTimeMillis())
                .build();

        log.info("Dispatching daily yield for asset {}: ${} total", GOLD_ASSET_ID, totalYield);
        kafkaTemplate.send("yield-distributions", GOLD_ASSET_ID.toString(), event);
    }
}
