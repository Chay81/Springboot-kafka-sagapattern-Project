package com.inventory.config;

import com.inventory.DTO.Order;
import com.inventory.repository.InventoryRepository;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryListener {

    private final InventoryService inventoryService;

    /**
     * Single Method for normal and Retry + DLQ enabled processing for testing
     */
    @KafkaListener(
            topics = "inventory-topic",
            groupId = "inventory-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeEventOrder(Order order) {
        log.info("✅ Consumed order event: {}", order);

        if ("fail-test".equalsIgnoreCase(order.getProductName())) {
            log.warn("❌ Simulated failure for retry/DLQ test");
            throw new RuntimeException("Simulated failure");
        }

        // Only update stock if no failure occurred

        // If no error, process normally
        inventoryService.updateStock(order.getProductName(), order.getQuantity());
        log.info("✅ Inventory updated successfully for: {}", order.getProductName());
    }
}
