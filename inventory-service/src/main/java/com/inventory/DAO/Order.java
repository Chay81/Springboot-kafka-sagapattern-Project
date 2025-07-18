package com.inventory.DAO;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String productName;
    private int quantity;
    private double price;
    private String brandName;
    private String modelNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

}