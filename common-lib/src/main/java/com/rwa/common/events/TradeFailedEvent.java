package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@lombok.Value
@Builder
@AllArgsConstructor
public class TradeFailedEvent {
    UUID tradeId;
    UUID buyerId;
    UUID assetId;
    java.math.BigDecimal fractions;
    java.math.BigDecimal priceAmount;
    String reason;
}
