package com.inventory.controller;

import com.inventory.entity.Inventory;
import com.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{product}")
    public ResponseEntity<Inventory> getStock(@PathVariable String product) {
        return inventoryService.getStock(product)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
