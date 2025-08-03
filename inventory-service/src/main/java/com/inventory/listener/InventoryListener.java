package com.inventory.listener;

import com.inventory.AppConstants;
import com.inventory.DAO.Order;
import com.inventory.DAO.OrderStatus;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
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
                log.warn(AppConstants.DLQ);
                throw new RuntimeException(AppConstants.DLQ);
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
                log.info(AppConstants.INVENTORY_UPDATED);
            } else {
                order.setStatus(OrderStatus.ORDER_FAILED);
                kafkaTemplate.send("order-compensation-topic", order);
                log.warn(AppConstants.INVENTORY_FAILED);
            }


        } catch (Exception e) {
            // Send to compensation topic on exception
            order.setStatus(OrderStatus.ORDER_FAILED);
            kafkaTemplate.send("order-compensation-topic", order);
            log.error(AppConstants.INVENTORY_EXCEPTION, e.getMessage(), e);
            throw e; // allow retry/DLQ if configured
        }
    }

//    @KafkaListener(
//            topics = "inventory-topic",
//            groupId = "inventory-group",
//            containerFactory = "kafkaListenerContainerFactory"
//    )
//    public void consumeEventOrder(Order order, Acknowledgment ack, Consumer<?, ?> consumer) {
//        log.info("Partitions assigned: {}", consumer.assignment());
//    }

}