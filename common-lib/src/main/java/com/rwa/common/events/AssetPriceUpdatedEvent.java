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
public class AssetPriceUpdatedEvent {
    private UUID assetId;
    private BigDecimal newPrice;
    private long timestamp;
}
