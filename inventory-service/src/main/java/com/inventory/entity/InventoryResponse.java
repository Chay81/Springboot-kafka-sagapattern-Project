package com.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class InventoryResponse {
    private String message;
    private Inventory inventory;

}
