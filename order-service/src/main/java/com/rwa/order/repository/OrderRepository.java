package com.rwa.order.repository;

import com.rwa.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByAssetIdAndTypeAndStatus(UUID assetId, Order.OrderType type, Order.OrderStatus status);
}
