package com.inventory.listener;

import com.inventory.DAO.Order;
import com.inventory.DAO.OrderStatus;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryListener {

    private final InventoryService inventoryService;
    private final KafkaTemplate<String, Order> kafkaTemplate;

    @KafkaListener(
            topics = "inventory-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEventOrder(Order order) {
        log.info("✅ Consumed order event: {}", order);

        try {
            if ("fail-test".equalsIgnoreCase(order.getProductName())) {
                log.warn("❌ Simulated failure for retry/DLQ test");
                throw new RuntimeException("Simulated failure");
            }

            // ✅ You must update this method to return boolean in InventoryServiceImpl
            boolean stockUpdated = inventoryService.updateStock(
                    order.getProductName(),
                    order.getQuantity(),
                    order.getPrice(),
                    order.getBrandName(),
                    order.getModelNumber()
            );

            if (stockUpdated) {
                order.setStatus(OrderStatus.INVENTORY_UPDATED);
                kafkaTemplate.send("order-response-topic", order);
                log.info("✅ Inventory updated, sent status INVENTORY_UPDATED");
            } else {
                order.setStatus(OrderStatus.ORDER_FAILED);
                kafkaTemplate.send("order-compensation-topic", order);
                log.warn("❌ Inventory update failed, sent status ORDER_FAILED to compensation topic");
            }


        } catch (Exception e) {
            // Send to compensation topic on exception
            order.setStatus(OrderStatus.ORDER_FAILED);
            kafkaTemplate.send("order-compensation-topic", order);
            log.error("🔥 Exception while processing inventory, sent to compensation: {}", e.getMessage(), e);
            throw e; // allow retry/DLQ if configured
        }
    }
}