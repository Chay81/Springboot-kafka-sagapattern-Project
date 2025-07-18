package com.inventory.listener;

import com.inventory.DAO.Order;
import com.inventory.DAO.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DLQListener {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    public DLQListener(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "orders-dlt", groupId = "dlq-group")
    public void handleDLQ(Order failedOrder) {
        log.error("🔥 DLQ received for order ID: {}", failedOrder.getOrderId());
        log.debug("📦 DLQ payload: {}", failedOrder);

        // Emit compensating event
        failedOrder.setStatus(OrderStatus.ORDER_FAILED);
        kafkaTemplate.send("order-compensation-topic", failedOrder);
        log.info("↩️ Compensation event sent for order ID: {}", failedOrder.getOrderId());
    }
}
