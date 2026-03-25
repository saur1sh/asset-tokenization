package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.Value
@Builder
@AllArgsConstructor
public class TradeInitiatedEvent {
    private UUID tradeId;
    private UUID buyerId;
    private UUID assetId;
    private BigDecimal fractions;
    private BigDecimal priceAmount;
}
