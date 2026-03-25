package com.rwa.asset.entity;

import jakarta.persistence.*;
import lombok.*;
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
