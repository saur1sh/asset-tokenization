package com.rwa.wallet.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {
    @Id
    private UUID userId;
    
    private BigDecimal balance;
    
    private String currency;
    
    private String kycStatus; // UNVERIFIED, VERIFIED, SUSPENDED

    @Version
    private Long version;
}
