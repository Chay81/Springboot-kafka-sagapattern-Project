package com.customer.loginmodels;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private String message;
    private String token;
    private String expiresAt;
    private String refreshToken;
    private String emailAddress; // masked in response in AuthServiceImpl
    private String phoneNumber;  // masked in response in AuthServiceImpl
}

