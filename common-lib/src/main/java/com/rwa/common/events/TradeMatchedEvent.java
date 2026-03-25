package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeMatchedEvent {
    private UUID tradeId;
    private UUID buyerId;
    private UUID sellerId;
    private UUID assetId;
    private BigDecimal quantity;
    private BigDecimal price;
    private long timestamp;
}
