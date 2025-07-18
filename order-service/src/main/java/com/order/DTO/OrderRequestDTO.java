package com.order.DTO;

import com.order.entity.OrderStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(max = 100, message = "Product name must not exceed 100 characters")
    private String productName;

    @Positive(message = "Quantity must be greater than 0")
    private int quantity;

    @PositiveOrZero(message = "Price cannot be negative")
    private double price;

    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brandName;

    @NotBlank(message = "Model number is required")
    @Size(max = 100, message = "Model number must not exceed 100 characters")
    private String modelNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
}

