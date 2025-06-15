package com.order.service;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.order.entity.Order;
import com.order.repository.OrderRepository;

@Service
@Slf4j
public class OrderService {

	private static final String TOPIC = "orders";

	@Autowired
	private KafkaTemplate<String, Order> kafkaTemplate;

	@Autowired
	private OrderRepository orderRepository;

	public Order placeOrder(Order order) {

		try {
			log.info("Placing order: {}", order);
			Order savedOrder = orderRepository.save(order);
			log.info("Order saved: {}", savedOrder);

			kafkaTemplate.send(TOPIC, order);

			// Prepare the event to be sent to InventoryService
			Order orderEvent = new Order();
			orderEvent.setOrderId(savedOrder.getOrderId());
			orderEvent.setProductName(savedOrder.getProductName());
			orderEvent.setQuantity(savedOrder.getQuantity());
			orderEvent.setStatus("ORDER_CREATED");

			// ✅ Send to Kafka topic
			kafkaTemplate.send("inventory-topic", orderEvent);

			return savedOrder;
		}catch (Exception e) {
			log.error("Error placing order: {}", e.getMessage());
			throw new RuntimeException("Failed to place order", e);
		}
	}

	public Order getByOrderId(Long orderId) {

		Optional<Order> optionalOrder = orderRepository.findByOrderId(orderId);
		if (optionalOrder.isPresent()) {
			return optionalOrder.get();
		} else {
			throw new RuntimeException("Order not found");
		}
	}
}