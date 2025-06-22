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
public class CompensationListener {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "order-compensation-topic", groupId = "order-group")
    public void handleCompensation(Order failedOrder) {
        log.warn("🔁 Compensation triggered for order ID: {}", failedOrder.getOrderId());

        orderRepository.findByOrderId(failedOrder.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.ORDER_FAILED);
            orderRepository.save(order);
            log.info("✅ Order status updated to ORDER_FAILED for order ID: {}", order.getOrderId());
        });
    }
}
