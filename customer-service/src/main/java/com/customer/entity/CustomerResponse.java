package com.customer.entity;

import com.customer.DTO.CustomerDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerResponse {
        private String message;
        private CustomerDTO customer;

}
// Note: these are internal classes, This is a response DTO, not used for incoming requests.
// Validation annotations like @NotBlank, @Valid etc. are not required here.