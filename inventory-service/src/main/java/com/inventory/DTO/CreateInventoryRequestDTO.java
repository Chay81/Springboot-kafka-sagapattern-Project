package com.inventory.DTO;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequestDTO {

    @NotBlank(message = "Product name is required")
    private String productName;

    @NotBlank(message = "Brand name is required")
    private String brandName;

    @NotBlank(message = "Model number is required")
    private String modelNumber;

    @Min(value = 0, message = "Stock must be 0 or greater")
    private int stock;

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    @Positive
    private double price;
}

