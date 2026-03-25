package com.rwa.settlement.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.FundsLockedEvent;
import com.rwa.common.events.InventoryReservedEvent;
import com.rwa.settlement.service.SettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEventConsumer {

    private final SettlementService settlementService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = RwaConstants.TOPIC_WALLET_EVENTS, groupId = RwaConstants.GROUP_SETTLEMENT_SERVICE)
    public void consumeFundsLockedEvent(String message) {
        try {
            FundsLockedEvent event = objectMapper.readValue(message, FundsLockedEvent.class);
            log.info("Received FundsLockedEvent: {}", event.getTradeId());
            settlementService.handleFundsLocked(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message as FundsLockedEvent: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing FundsLockedEvent", e);
        }
    }

    @KafkaListener(topics = RwaConstants.TOPIC_INVENTORY_EVENTS, groupId = RwaConstants.GROUP_SETTLEMENT_SERVICE)
    public void consumeInventoryReservedEvent(String message) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(message, InventoryReservedEvent.class);
            log.info("Received InventoryReservedEvent: {}", event.getTradeId());
            settlementService.handleInventoryReserved(event);
        } catch (JsonProcessingException e) {
            log.error("Error deserializing message as InventoryReservedEvent: {}", message, e);
        } catch (Exception e) {
            log.error("Error processing InventoryReservedEvent", e);
        }
    }
}
