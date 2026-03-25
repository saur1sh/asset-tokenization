package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@lombok.Value
@Builder
@AllArgsConstructor
public class TradeFailedEvent {
    private UUID tradeId;
    private UUID buyerId;
    private UUID assetId;
    private java.math.BigDecimal fractions;
    private java.math.BigDecimal priceAmount;
    private String reason;
}
