package com.customer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResponse {
    private boolean success;
    private String message;
}

// Note: these are internal classes, This is a response DTO, not used for incoming requests.
// Validation annotations like @NotBlank, @Valid etc. are not required here.