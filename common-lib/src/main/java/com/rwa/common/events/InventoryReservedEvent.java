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
public class InventoryReservedEvent {
    private UUID tradeId;
    private UUID assetId;
    private BigDecimal fractionsReserved;
    private String status; // e.g., RESERVED, FAILED
}
