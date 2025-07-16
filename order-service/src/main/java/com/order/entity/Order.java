package com.order.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false)
    private String emailAddress; // Added: To track which customer placed the order

    private String productName;
    private int quantity;
    private double price;
    private String brandName;
    private String modelNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return quantity == order.quantity && Double.compare(price, order.price) == 0
                && Objects.equals(orderId, order.orderId)
                && Objects.equals(productName, order.productName)
                && Objects.equals(brandName, order.brandName)
                && Objects.equals(modelNumber, order.modelNumber)
                && status == order.status
                && Objects.equals(emailAddress, order.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId, productName, quantity, price, brandName, modelNumber, status, emailAddress);
    }
}