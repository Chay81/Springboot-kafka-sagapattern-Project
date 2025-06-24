package com.customer.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerResponse {
        private String message;
        private Customer customer;

    public CustomerResponse(String message) {
    }
}
