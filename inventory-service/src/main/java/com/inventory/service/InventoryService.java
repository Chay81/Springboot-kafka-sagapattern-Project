package com.inventory.service;

import com.inventory.DTO.CreateInventoryRequestDTO;
import com.inventory.entity.Inventory;
import com.inventory.entity.InventoryResponse;

import java.util.List;
import java.util.Optional;

public interface InventoryService {

    public boolean updateStock(String productName, int quantity, double price, String brandName, String modelNumber);

    Optional<Inventory> getStock(String brandName, String modelNumber);

    List<Inventory> getProducts(String productName);

    InventoryResponse createInventory(CreateInventoryRequestDTO requestDTO);

    boolean isStockAvailable(String brandName, String modelNumber, int quantity);
}
