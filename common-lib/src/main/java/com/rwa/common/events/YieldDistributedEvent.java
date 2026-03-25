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
public class YieldDistributedEvent {
    private UUID assetId;
    private BigDecimal totalYieldAmount;
    private BigDecimal amountPerFraction;
    private long timestamp;
}
