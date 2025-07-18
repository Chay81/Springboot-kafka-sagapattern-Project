package com.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @GetMapping("/inventory/check")
    boolean isStockAvailable(
            @RequestParam("brandName") String brandName,
            @RequestParam("modelNumber") String modelNumber,
            @RequestParam("quantity") int quantity
    );
}


