package com.rwa.asset.entity;

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
public class DigitalAsset {
    
    @Id
    private UUID id;
    
    private String type;
    
    private BigDecimal totalFractions;
    
    private BigDecimal remainingFractions;
    
    private BigDecimal price;
    
    @Version
    private Long version;
}
