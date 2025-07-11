package com.customer.entity;

import com.customer.DTO.CustomerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerResponse {
        private String message;
        private CustomerDTO customer;

    public CustomerResponse(String message) {
    }
}
