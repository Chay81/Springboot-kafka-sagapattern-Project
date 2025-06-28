package com.customer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

// Response DTO
@Data
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String token;
}
