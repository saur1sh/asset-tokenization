package com.rwa.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet_locks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletLock {

    @Id
    private UUID tradeId;

    private UUID userId;

    private BigDecimal amount;

    private String status; // LOCKED, RELEASED

    private LocalDateTime createdAt;
}
