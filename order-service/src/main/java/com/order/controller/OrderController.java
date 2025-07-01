package com.order.controller;

import com.order.constants.AppConstants;
import com.order.entity.OrderStatus;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody Order order) {

        log.info(AppConstants.INCOMING_ORDER, order);
        boolean available = orderService.checkStockAvailability(order);

        if (!available) {
            Order failedOrder = orderService.placeOrder(order); // Save the failed order

            log.warn(AppConstants.INVENTORY_NOT_AVAILABLE);
            order.setStatus(OrderStatus.ORDER_FAILED);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new OrderResponse(AppConstants.ORDER_FAILED, failedOrder));
        }

        log.info("Stock is available. Proceeding to place order.");
        Order created = orderService.placeOrder(order);
        OrderResponse response = new OrderResponse(AppConstants.ORDER_RESPONSE, created);
        OrderResponse DLQResponse = new OrderResponse(AppConstants.ORDER_DLQ, created);

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