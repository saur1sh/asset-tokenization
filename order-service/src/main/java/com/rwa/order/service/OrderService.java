package com.rwa.order.service;

import com.rwa.common.constants.RwaConstants;
import com.rwa.common.events.TradeMatchedEvent;
import com.rwa.order.entity.Order;
import com.rwa.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public Order placeOrder(Order order) {
        order.setStatus(Order.OrderStatus.PENDING);
        order.setTimestamp(System.currentTimeMillis());
        Order savedOrder = orderRepository.save(order);
        
        log.info("Placed {} order for asset {} by user {}: {} units @ {}", 
                order.getType(), order.getAssetId(), order.getUserId(), order.getQuantity(), order.getPrice());
                
        matchOrder(savedOrder);
        return savedOrder;
    }

    private void matchOrder(Order newOrder) {
        Order.OrderType targetType = (newOrder.getType() == Order.OrderType.BID) ? Order.OrderType.ASK : Order.OrderType.BID;
        
        List<Order> candidates = orderRepository.findByAssetIdAndTypeAndStatus(
                newOrder.getAssetId(), targetType, Order.OrderStatus.PENDING);
                
        for (Order candidate : candidates) {
            boolean priceMatch = (newOrder.getType() == Order.OrderType.BID) 
                    ? newOrder.getPrice().compareTo(candidate.getPrice()) >= 0 
                    : newOrder.getPrice().compareTo(candidate.getPrice()) <= 0;
            
            if (priceMatch) {
                // Simplified: Match exact quantity for this demo
                log.info("Match found between Order {} and Order {}", newOrder.getId(), candidate.getId());
                
                newOrder.setStatus(Order.OrderStatus.MATCHED);
                candidate.setStatus(Order.OrderStatus.MATCHED);
                
                orderRepository.save(newOrder);
                orderRepository.save(candidate);
                
                TradeMatchedEvent event = TradeMatchedEvent.builder()
                        .buyerId(newOrder.getType() == Order.OrderType.BID ? newOrder.getUserId() : candidate.getUserId())
                        .sellerId(newOrder.getType() == Order.OrderType.ASK ? newOrder.getUserId() : candidate.getUserId())
                        .assetId(newOrder.getAssetId())
                        .quantity(newOrder.getQuantity())
                        .price(candidate.getPrice()) // Execution price is the existing order's price
                        .timestamp(System.currentTimeMillis())
                        .build();
                        
                kafkaTemplate.send(RwaConstants.TOPIC_TRADE_MATCHES, event.getAssetId().toString(), event);
                break; 
            }
        }
    }
}
