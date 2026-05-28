package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.Value
@Builder
@AllArgsConstructor
public class TradeInitiatedEvent {
    UUID tradeId;
    UUID buyerId;
    UUID assetId;
    BigDecimal fractions;
    BigDecimal priceAmount;
}
