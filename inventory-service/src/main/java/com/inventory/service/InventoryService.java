package com.inventory.service;

import com.inventory.entity.Inventory;

import java.util.List;
import java.util.Optional;

public interface InventoryService {

    public boolean updateStock(String productName, int quantity, double price, String brandName, String modelNumber);

    Optional<Inventory> getStock(String brandName, String modelNumber);

    List<Inventory> getProducts(String productName);

    Inventory createStock(Inventory inventory);
}
