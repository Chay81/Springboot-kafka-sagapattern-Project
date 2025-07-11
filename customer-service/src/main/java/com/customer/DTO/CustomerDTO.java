package com.customer.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDTO {

    private String customerId;  // UUID-style or masked

    private String customerName;
    private String phoneNumber;
    private String emailAddress;
    private boolean sameAddress;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    private List<AddressDTO> billingAddress;
    private List<AddressDTO> shippingAddress;
    private List<String> roles; // Optional: Will default to ROLE_CUSTOMER
}

