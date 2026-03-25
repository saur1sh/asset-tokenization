package com.rwa.wallet.event;

import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.YieldDistributedEvent;
import com.rwa.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class YieldEventConsumer {

    private final WalletService walletService;

    @KafkaListener(topics = RwaConstants.TOPIC_YIELD_EVENTS, groupId = RwaConstants.GROUP_WALLET_SERVICE)
    public void consumeYieldUpdate(YieldDistributedEvent event) {
        log.info("Received yield distribution for asset {}: {} per fraction", event.getAssetId(), event.getAmountPerFraction());
        
        // In a real system, we'd query all holders of this asset.
        // For this demo, we'll credit a "Mock User" who holds 10 fractions.
        BigDecimal mockAmount = event.getAmountPerFraction().multiply(new BigDecimal("10"));
        // walletService.creditYield(MOCK_USER_ID, mockAmount);
    }
}
