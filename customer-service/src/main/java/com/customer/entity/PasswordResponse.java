package com.customer.entity;

import com.customer.DTO.CustomerDTO;
import com.customer.DTO.ForgotPasswordRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasswordResponse {
    private boolean success;
    private String message;
}
