package com.inventory.controller;

import com.inventory.entity.Inventory;
import com.inventory.entity.InventoryResponse;
import com.inventory.exceptions.ResourceNotFoundException;
import com.inventory.repository.InventoryRepository;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @GetMapping("/{brandName}/{modelNumber}")
    public ResponseEntity<Inventory> getStock(@PathVariable String brandName, @PathVariable String modelNumber) {
            return inventoryService.getStock(brandName, modelNumber)
                    .map(ResponseEntity::ok)
                    .orElseThrow(() -> new ResourceNotFoundException
                            ("Inventory not found for brandName: " + brandName + ", modelNumber: " + modelNumber));
    }

    @GetMapping("/product/{productName}")
    public ResponseEntity <List<Inventory>> getInventory(@PathVariable String productName) {
        return ResponseEntity.ok(inventoryService.getProducts(productName));
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@RequestBody Inventory inventory) {

        log.info(" Creating inventory for: {}", inventory);
        Optional<Inventory> existing = inventoryService.getStock(inventory.getBrandName(), inventory.getModelNumber());

        Inventory createdInventory = inventoryService.createStock(inventory);

        // check if the inventory already exists, if inventory exists, return the existedInventory response else create a new inventory and return the response
        InventoryResponse response = existing.isPresent()
                ? new InventoryResponse("Inventory already exists, stock updated", createdInventory)
                : new InventoryResponse("Inventory created successfully!", createdInventory);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isStockAvailable(@RequestParam String brand,
                                                    @RequestParam String model,
                                                    @RequestParam int quantity) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findByBrandNameAndModelNumber(brand, model);
        if (inventoryOpt.isPresent()) {
            return ResponseEntity.ok(inventoryOpt.get().getStock() >= quantity);
        }
        return ResponseEntity.ok(false);
    }



}
