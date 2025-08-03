package com.order.listener;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderResponseListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-response-topic", groupId = "order-group")
    public void handleOrderResponse(Order updatedOrder) {
        log.info("📦 Received updated order from inventory: {}", updatedOrder.getOrderId());

        orderRepository.findByOrderId(updatedOrder.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.ORDER_PLACED); // typically INVENTORY_UPDATED
            orderRepository.save(order);
            log.info("✅ Order status updated to: {} for orderId: {}", updatedOrder.getStatus(), updatedOrder.getOrderId());
        });
    }
}

