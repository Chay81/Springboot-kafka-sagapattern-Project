package com.inventory.service;

import com.inventory.entity.Inventory;
import com.inventory.exceptions.ResourceNotFoundException;
import com.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public boolean updateStock(String productName, int quantity, double price, String brandName, String modelNumber) {
        log.info("Updating stock for product: {}, quantity: {}, price: {}", productName, quantity, price);

        try {
            Optional<Inventory> optionalInventory = inventoryRepository.findByBrandNameAndModelNumber(brandName, modelNumber);

            if (optionalInventory.isEmpty()) {
                log.warn("❌ Inventory not found for brand: {}, model: {}", brandName, modelNumber);
                return false;
            }

            Inventory inventory = optionalInventory.get();

            if (inventory.getStock() < quantity) {
                log.warn("❌ Insufficient stock for product: {}, available: {}, requested: {}",
                        productName, inventory.getStock(), quantity);
                return false;
            }

            inventory.setStock(inventory.getStock() - quantity); // Deduct stock
            inventory.setPrice(price); // Update price if required

            inventoryRepository.save(inventory);
            log.info("✅ Stock updated successfully for brand: {}, model: {}", brandName, modelNumber);
            return true;

        } catch (Exception e) {
            log.error("🔥 Error updating inventory: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Optional<Inventory> getStock(String brandName, String modelNumber) {
        log.info("Looking up inventory for brand: {} and model: {}", brandName, modelNumber);
        return inventoryRepository.findByBrandNameAndModelNumber(brandName, modelNumber);
    }

    @Override
    public List<Inventory> getProducts(String productName) {
        List<Inventory> inventoryProducts = inventoryRepository.findByProductName(productName);
        if (inventoryProducts.isEmpty()) {
            throw new ResourceNotFoundException("No inventory found for the product : " + productName);
        }
        return inventoryProducts;
    }

    @Override
    public Inventory createStock(Inventory inventory) {

        log.info("Creating or updating inventory for brand: {}, model: {}", inventory.getBrandName(), inventory.getModelNumber());

        Optional<Inventory> existingInventory = inventoryRepository
                .findByBrandNameAndModelNumber(inventory.getBrandName(), inventory.getModelNumber());

        if (existingInventory.isPresent()) {
            Inventory existing = existingInventory.get();
            existing.setStock(existing.getStock() + inventory.getStock()); // ✅ Add stock
            existing.setPrice(inventory.getPrice());
            log.info(" Existing inventory updated: {}", existing);
            return inventoryRepository.save(existing);
        } else {
            log.info(" New inventory created: {}", inventory);
            return inventoryRepository.save(inventory);
        }

    }
}
