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
public class FundsLockedEvent {
    private UUID tradeId;
    private UUID userId;
    private BigDecimal amount;
    private String status; // e.g., LOCKED, FAILED
}
