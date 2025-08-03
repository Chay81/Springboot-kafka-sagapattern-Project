package com.order.service;

import com.order.DTO.OrderRequestDTO;
import com.order.client.InventoryClient;
import com.order.constants.AppConstants;
import com.order.entity.Order;
import com.order.entity.OrderStatus;
import com.order.exceptions.ResourceNotFoundException;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private InventoryClient inventoryClient;

    public Order placeOrder(OrderRequestDTO orderRequestDTO, String emailAddress) {

        try {
            log.info("Placing order: {}", orderRequestDTO);

            // 🏗️ Map DTO to Entity
            Order order = Order.builder()
                    .productName(orderRequestDTO.getProductName())
                    .quantity(orderRequestDTO.getQuantity())
                    .price(orderRequestDTO.getPrice())
                    .brandName(orderRequestDTO.getBrandName())
                    .modelNumber(orderRequestDTO.getModelNumber())
                    .emailAddress(emailAddress) // Associate with logged-in customer
                    .build();
            boolean available = checkStockAvailability(order);

            if (!available) {
                Order failedOrder = orderRepository.save(order);
                log.warn(AppConstants.INVENTORY_NOT_AVAILABLE);
                order.setStatus(OrderStatus.ORDER_FAILED);

                // Still send failed order to Kafka
                kafkaTemplate.send(INVENTORY_TOPIC, failedOrder);
                log.info("📤 Sent failed order to Kafka");

                return failedOrder;
            }

//          // Normal successful order flow
            order.setStatus(OrderStatus.ORDER_PLACED);
            Order placedOrder  = orderRepository.save(order);
            log.info("Order saved: {}", placedOrder );

            // Send to Kafka topic
            kafkaTemplate.send(INVENTORY_TOPIC, placedOrder );
            log.info("Order event sent to inventory-topic");
            log.info("Order Status is: {}", placedOrder.getStatus());
            return placedOrder;

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
        log.info("Checking stock availability using FeignClient for order: {}", order);

        try {
            return inventoryClient.isStockAvailable(
                    order.getBrandName(),
                    order.getModelNumber(),
                    order.getQuantity()
            );
        } catch (Exception e) {
            log.error("Feign call to inventory-service failed: {}", e.getMessage());
            return false;
        }
    }


    /* @Override
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
    } */
}
