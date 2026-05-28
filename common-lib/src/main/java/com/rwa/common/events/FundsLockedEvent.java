package com.rwa.common.events;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@lombok.Value
@Builder
@AllArgsConstructor
public class FundsLockedEvent {
    UUID tradeId;
    UUID userId;
    BigDecimal amount;
    String status; // e.g., LOCKED, FAILED
}
