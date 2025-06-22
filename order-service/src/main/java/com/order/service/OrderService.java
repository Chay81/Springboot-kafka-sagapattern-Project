package com.order.service;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.order.entity.Order;
import com.order.repository.OrderRepository;

public interface OrderService {

	public Order placeOrder(Order order);

	public Order getByOrderId(Long orderId);

	List<Order> getStockOrders(String productName);

	boolean checkStockAvailability(Order order);
}