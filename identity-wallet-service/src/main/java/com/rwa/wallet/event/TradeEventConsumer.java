package com.rwa.wallet.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.TradeFailedEvent;
import com.rwa.common.events.TradeInitiatedEvent;
import com.rwa.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TradeEventConsumer {

    private final WalletService walletService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = RwaConstants.TOPIC_TRADE_INITIATED, groupId = RwaConstants.GROUP_WALLET_TRADE)
    public void consumeTradeInitiatedEvent(String message) {
        try {
            TradeInitiatedEvent event = objectMapper.readValue(message, TradeInitiatedEvent.class);
            log.info("Received TradeInitiatedEvent: {}", event.getTradeId());
            walletService.lockFunds(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing message", e);
            // Error handling/retry policy would be configured here
        }
    }

    @KafkaListener(topics = RwaConstants.TOPIC_TRADE_FAILED, groupId = RwaConstants.GROUP_WALLET_TRADE)
    public void consumeTradeFailedEvent(String message) {
        try {
            TradeFailedEvent event = objectMapper.readValue(message, TradeFailedEvent.class);
            log.info("Received TradeFailedEvent: {} for user {}. Reason: {}", 
                    event.getTradeId(), event.getBuyerId(), event.getReason());
            
            if (event.getPriceAmount() != null && event.getPriceAmount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                walletService.refundFunds(event.getBuyerId(), event.getPriceAmount(), event.getTradeId());
            }
        } catch (Exception e) {
            log.error("Error processing TradeFailedEvent", e);
        }
    }
}
