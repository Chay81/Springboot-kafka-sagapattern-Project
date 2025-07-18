package com.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class InventoryResponse {
    private String message;
    private String productName;
    private String brandName;
    private String modelNumber;
    private double price;
    private int stock;
}
