package com.customer.loginmodels;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}

