package com.inventory.listener;

import com.inventory.DTO.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DLQListener {

    @KafkaListener(topics = "orders-dlt", groupId = "dlq-group")
    public void handleDLQ(Order failedOrder) {
        log.info("🔥 Message received in DLQ: " + failedOrder);
        // Optional: Save to DB, send alerts, etc.
    }
}

