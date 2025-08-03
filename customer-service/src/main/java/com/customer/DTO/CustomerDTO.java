package com.customer.DTO;

import com.customer.entity.Customer;
import com.customer.util.DataMaskingUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Customer name is required")
    @Size(max = 100, message = "Customer name must be at most 100 characters")
    private String customerName;


    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^[6-9]\\d{9}$", // Indian mobile format (customize if needed)
            message = "Phone number must be valid"
    )
    private String phoneNumber;

    @NotBlank(message = "Email address is required")
    @Email(message = "Invalid email format")
    private String emailAddress;

    private boolean sameAddress;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    private List<AddressDTO> billingAddress;

    private List<AddressDTO> shippingAddress;
    private List<String> roles; // Optional: Will default to ROLE_CUSTOMER

    @Size(min = 8, max = 100, message = "New password must be between 8 and 100 characters")
    private String newPassword;

    @Size(min = 8, max = 100, message = "Retyped password must be between 8 and 100 characters")
    private String retypePassword;

}

