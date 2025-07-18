package com.order.service;

import com.order.DTO.OrderRequestDTO;
import com.order.entity.Order;

import java.util.List;
import java.util.Set;

public interface OrderService {

	Order placeOrder(OrderRequestDTO dto, String emailAddress);

	Order getByOrderId(Long orderId, String emailAddress, Set<String> roles);

	List<Order> getProductOrders(String productName, String emailAddress, Set<String> roles);

	boolean checkStockAvailability(Order order);
}