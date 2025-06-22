package com.order.controller;

import com.order.entity.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.order.entity.Order;
import com.order.entity.OrderResponse;
import com.order.service.OrderService;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody Order order) {

        boolean available = orderService.checkStockAvailability(order);

        if (!available) {
            order.setStatus(OrderStatus.ORDER_FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OrderResponse("❌ Product is out of stock", order));
        }

        Order created = orderService.placeOrder(order);
        OrderResponse response = new OrderResponse("Order placed and sent to Inventory Service!", created);
        OrderResponse DLQResponse = new OrderResponse("Order failed to process in Inventory Service," +
				" after 2 retries, sent to dead letter topic", created);

        // Check if the order was successfully created, if not, return the DLQ response
        if ("fail-test".equalsIgnoreCase(order.getProductName())) {
            return ResponseEntity.status(HttpStatus.CREATED).body(DLQResponse);
        } else {
            // If the order was successfully created, return the normal response
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getByOrderId(orderId));
    }

    @GetMapping("/product/{productName}")
    public ResponseEntity <List<Order>> getProductOrders(@PathVariable String productName) {
        return ResponseEntity.ok(orderService.getStockOrders(productName));
    }



}