package com.customer.loginmodels;

import lombok.Data;

// Request DTO
@Data
public class LoginRequest {
    private String email;
    private String password;
}

