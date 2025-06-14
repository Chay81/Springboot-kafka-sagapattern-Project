package com.order.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.order.entity.Order;
import com.order.repository.OrderRepository;

@Service
public class OrderService {

	private static final String TOPIC = "orders";

	@Autowired
	private KafkaTemplate<String, Order> kafkaTemplate;

	@Autowired
	private OrderRepository orderRepository;

	public Order placeOrder(Order order) {
		Order saved = orderRepository.save(order);
		kafkaTemplate.send(TOPIC, order);
		return saved;
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