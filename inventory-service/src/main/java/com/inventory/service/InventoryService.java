package com.inventory.service;

import com.inventory.entity.Inventory;
import com.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public void updateStock(String productName, int quantity) {
        Inventory inventory = inventoryRepository.findByProductName(productName)
                .orElse(Inventory.builder().productName(productName).stock(0).build());
        inventory.setStock(quantity - inventory.getStock());
        inventoryRepository.save(inventory);
    }

    public Optional<Inventory> getStock(String productName) {
        return inventoryRepository.findByProductName(productName);
    }
}
