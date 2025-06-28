package com.customer.entity;

import lombok.Data;

// Request DTO
@Data
public class LoginRequest {
    private String email;
    private String password;
}

