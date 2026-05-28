package com.rwa.order.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private UUID userId;
    private UUID assetId;

    @Enumerated(EnumType.STRING)
    private OrderType type; // BID (Buy) or ASK (Sell)

    private BigDecimal price;
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, MATCHED, CANCELLED

    private Long timestamp;

    public enum OrderType {
        BID, ASK
    }

    public enum OrderStatus {
        PENDING, MATCHED, CANCELLED
    }
}
