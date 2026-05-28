package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.Value
@Builder
@AllArgsConstructor
public class InventoryReservedEvent {
    UUID tradeId;
    UUID assetId;
    BigDecimal fractionsReserved;
    String status; // e.g., RESERVED, FAILED
}
