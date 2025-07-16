package com.order.service;

import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.exceptions.ResourceNotFoundException;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private static final String INVENTORY_TOPIC = "inventory-topic";

    @Autowired
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;


    public Order placeOrder(Order order) {

        try {
            log.info("Placing order: {}", order);

//          placing order
            order.setStatus(OrderStatus.ORDER_PLACED);
            Order savedOrder = orderRepository.save(order);
            log.info("Order saved: {}", savedOrder);

            // Send to Kafka topic
            kafkaTemplate.send(INVENTORY_TOPIC, savedOrder);
            log.info("Order event sent to inventory-topic");
            return savedOrder;

        } catch (Exception e) {
            log.error("Error placing order: {}", e.getMessage());
            throw new RuntimeException("Failed to place order", e);
        }
    }

    public Order getByOrderId(Long orderId, String emailAddress, Set<String> roles) {
        Order order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // 🔐 Only admins or owners can access the order
        if (!roles.contains("ROLE_ADMIN") && !emailAddress.equalsIgnoreCase(order.getEmailAddress())) {
            throw new AccessDeniedException("You are not authorized to view this order.");
        }

        return order;
    }


    @Override
    public List<Order> getProductOrders(String productName, String emailAddress, Set<String> roles) {
        log.info("📦 Retrieving orders for product: {}", productName);
        List<Order> orders = orderRepository.findByProductName(productName);
        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("No orders found for the order : " + productName);
        }

        // 🔐 If not admin, filter only the orders placed by this user
        if (!roles.contains("ROLE_ADMIN")) {
            orders = orders.stream()
                    .filter(order -> emailAddress.equalsIgnoreCase(order.getEmailAddress()))
                    .collect(Collectors.toList());

            if (orders.isEmpty()) {
                throw new AccessDeniedException("You are not authorized to view orders for this product.");
            }
        }
        return orders;
    }

    /*
        * This method checks the stock availability by calling the Inventory Service.
        * It constructs a URL with the brand name, model number, and quantity from the order,
        * and sends a GET request to the Inventory Service to check if the stock is available.
        * If the stock is available, it returns true; otherwise, it returns false.
    * */
    @Override
    public boolean checkStockAvailability(Order order) {

        log.info("Checking stock availability for order: {}", order);
        String url = "http://localhost:8082/inventory/check?brand=" + order.getBrandName() +
                "&model=" + order.getModelNumber() + "&quantity=" + order.getQuantity();

        try {
            ResponseEntity<Boolean> response = restTemplate.getForEntity(url, Boolean.class);
            return Boolean.TRUE.equals(response.getBody());
        } catch (Exception e) {
            log.error("Inventory check failed: {}", e.getMessage());
            return false;
        }
    }
}
