package com.inventory.controller;

import com.inventory.DTO.CreateInventoryRequestDTO;
import com.inventory.entity.Inventory;
import com.inventory.entity.InventoryResponse;
import com.inventory.exceptions.ResourceNotFoundException;
import com.inventory.repository.InventoryRepository;
import com.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Validated
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
    public ResponseEntity<InventoryResponse> createInventory(@RequestBody @Valid CreateInventoryRequestDTO requestDTO) {

        log.info("🎯 Creating inventory via controller: {}", requestDTO);
        InventoryResponse response = inventoryService.createInventory(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> isStockAvailable(@RequestParam String brandName,
                                                    @RequestParam String modelNumber,
                                                    @RequestParam int quantity) {
        log.info("🔍 Checking stock availability for brand={}, model={}, quantity={}", brandName, modelNumber, quantity);
        boolean isAvailable = inventoryService.isStockAvailable(brandName, modelNumber, quantity);
        return ResponseEntity.ok(isAvailable);
    }
}
