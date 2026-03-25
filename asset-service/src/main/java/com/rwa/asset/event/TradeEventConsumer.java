import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwa.asset.service.AssetService;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.TradeFailedEvent;
import com.rwa.common.events.TradeInitiatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventConsumer {

    private final AssetService assetService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = RwaConstants.TOPIC_TRADE_INITIATED, groupId = RwaConstants.GROUP_ASSET_TRADE)
    public void consumeTradeInitiatedEvent(String message) {
        try {
            TradeInitiatedEvent event = objectMapper.readValue(message, TradeInitiatedEvent.class);
            log.info("Received TradeInitiatedEvent: {}", event.getTradeId());
            assetService.reserveTokens(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing message", e);
            // Handling exceptions avoids infinite retries if no error handler is configured
            // In a production app with Resilience4j, we would define retry policies or DLTs
        }
    }

    @KafkaListener(topics = RwaConstants.TOPIC_TRADE_FAILED, groupId = RwaConstants.GROUP_ASSET_TRADE)
    public void consumeTradeFailedEvent(String message) {
        try {
            TradeFailedEvent event = objectMapper.readValue(message, TradeFailedEvent.class);
            log.info("Received TradeFailedEvent: {} for asset {}. Reason: {}", 
                    event.getTradeId(), event.getAssetId(), event.getReason());
            
            if (event.getFractions() != null && event.getFractions().compareTo(java.math.BigDecimal.ZERO) > 0) {
                assetService.releaseReservedFractions(event.getAssetId(), event.getFractions(), event.getTradeId());
            }
        } catch (Exception e) {
            log.error("Error processing TradeFailedEvent", e);
        }
    }
}
