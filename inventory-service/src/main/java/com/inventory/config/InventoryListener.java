package com.inventory.config;

import com.inventory.DTO.Order;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryListener {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "orders", groupId = "inventory-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Order order) {
        log.info("Received Order: {}", order);

        log.info("📦 Inventory updated for order: " + order.getOrderId() +
                ", Product: " + order.getProductName() +
                ", Quantity: " + order.getQuantity());

    }
}
