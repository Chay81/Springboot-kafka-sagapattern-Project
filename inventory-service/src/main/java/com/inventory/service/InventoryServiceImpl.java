package com.inventory.service;

import com.inventory.AppConstants;
import com.inventory.DTO.CreateInventoryRequestDTO;
import com.inventory.entity.Inventory;
import com.inventory.entity.InventoryResponse;
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
        log.info(AppConstants.UPDATING_STOCK, productName, quantity, price);

        try {
            Optional<Inventory> optionalInventory = inventoryRepository.findByBrandNameAndModelNumber(brandName, modelNumber);

            if (optionalInventory.isEmpty()) {
                log.warn(AppConstants.INVENTORY_NOT_FOUND, brandName, modelNumber);
                return false;
            }

            Inventory inventory = optionalInventory.get();

            if (inventory.getStock() < quantity) {
                log.warn(AppConstants.INSUFFICIENT_STOCK,
                        productName, inventory.getStock(), quantity);
                return false;
            }

            inventory.setStock(inventory.getStock() - quantity); // Deduct stock
            inventory.setPrice(price); // Update price if required

            inventoryRepository.save(inventory);
            log.info(AppConstants.STOCK_UPDATED, brandName, modelNumber);
            return true;

        } catch (Exception e) {
            log.error("🔥 Error updating inventory: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public Optional<Inventory> getStock(String brandName, String modelNumber) {
        log.info(AppConstants.GET_STOCK, brandName, modelNumber);
        return inventoryRepository.findByBrandNameAndModelNumber(brandName, modelNumber);
    }

    @Override
    public List<Inventory> getProducts(String productName) {
        List<Inventory> inventoryProducts = inventoryRepository.findByProductName(productName);
        if (inventoryProducts.isEmpty()) {
            throw new ResourceNotFoundException(AppConstants.NO_INVENTORY+ productName);
        }
        return inventoryProducts;
    }

    @Override
    public InventoryResponse createInventory(CreateInventoryRequestDTO requestDTO) {

        log.info(AppConstants.CREATE_INVENTORY, requestDTO.getBrandName(), requestDTO.getModelNumber());

        Optional<Inventory> existingInventory = inventoryRepository
                .findByBrandNameAndModelNumber(requestDTO.getBrandName(), requestDTO.getModelNumber());

        Inventory inventory;
        String message;

        if (existingInventory.isPresent()) {
            Inventory existing = existingInventory.get();
            existing.setStock(existing.getStock() + requestDTO.getStock()); // ✅ Add stock
            existing.setPrice(requestDTO.getPrice());

            inventory = inventoryRepository.save(existing);
            message = "Inventory already exists, stock updated";
            log.info("Existing inventory updated: {}", inventory);
        } else {
            inventory = Inventory.builder()
                    .productName(requestDTO.getProductName())
                    .brandName(requestDTO.getBrandName())
                    .modelNumber(requestDTO.getModelNumber())
                    .price(requestDTO.getPrice())
                    .stock(requestDTO.getStock())
                    .build();

            inventory = inventoryRepository.save(inventory);
            message = "Inventory created successfully!";
            log.info("New inventory created: {}", inventory);
        }
        return InventoryResponse.builder()
                .message(message)
                .productName(inventory.getProductName())
                .brandName(inventory.getBrandName())
                .modelNumber(inventory.getModelNumber())
                .price(inventory.getPrice())
                .stock(inventory.getStock())
                .build();
    }

    @Override
    public boolean isStockAvailable(String brandName, String modelNumber, int quantity) {
        return inventoryRepository.findByBrandNameAndModelNumber(brandName, modelNumber)
                .map(inventory -> hasSufficientStock(inventory, quantity))
                .orElse(false);
    }

    private boolean hasSufficientStock(Inventory inventory, int requiredQuantity) {
        boolean available = inventory.getStock() >= requiredQuantity;

        if (!available) {
            log.warn("❌ Insufficient stock: available={}, required={}", inventory.getStock(), requiredQuantity);
        } else {
            log.info("✅ Sufficient stock available: {}", inventory.getStock());
        }
        return available;
    }

}
