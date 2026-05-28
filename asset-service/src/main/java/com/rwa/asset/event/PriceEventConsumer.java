package com.rwa.asset.event;

import com.rwa.asset.service.AssetService;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.AssetPriceUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PriceEventConsumer {

    private final AssetService assetService;

    @KafkaListener(topics = RwaConstants.TOPIC_PRICE_EVENTS, groupId = RwaConstants.GROUP_ASSET_PRICE)
    public void consumePriceUpdate(AssetPriceUpdatedEvent event) {
        log.info("Received price update for asset {}: ${}", event.getAssetId(), event.getNewPrice());
        assetService.updateAssetPrice(event.getAssetId(), event.getNewPrice());
    }
}
