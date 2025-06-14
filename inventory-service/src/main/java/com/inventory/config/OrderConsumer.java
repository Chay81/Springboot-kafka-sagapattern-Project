package com.inventory.config;

import com.inventory.DTO.Order;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = "orders", groupId = "inventory-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(Order order) {
        inventoryService.updateStock(order.getProductName(), order.getQuantity());
    }
}
