package com.rwa.asset.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "asset_reservations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetReservation {

    @Id
    private UUID tradeId;

    private UUID assetId;

    private BigDecimal fractions;

    private String status; // RESERVED, CANCELLED

    private LocalDateTime createdAt;
}
