package com.rwa.settlement.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSaga {
    @Id
    private UUID id;
    private UUID buyerId;
    private UUID assetId;
    private BigDecimal fractions;
    private BigDecimal priceAmount;
    
    private String status; // PENDING, COMPLETED, FAILED, ROLLED_BACK
    private boolean fundsLocked;
    private boolean inventoryReserved;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}
