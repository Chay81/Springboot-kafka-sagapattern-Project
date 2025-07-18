package com.customer.DTO;

import com.customer.entity.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {

    private Long addressId;

    @Size(max = 100, message = "Apartment name must be at most 100 characters")
    private String apartmentName;

    @NotBlank(message = "Address Line 1 is mandatory")
    @Size(max = 150, message = "Address Line 1 must be at most 150 characters")
    private String addLine1;

    @Size(max = 150, message = "Address Line 2 must be at most 150 characters")
    private String addLine2;

    @NotBlank(message = "City is mandatory")
    @Size(max = 100, message = "City must be at most 100 characters")
    private String city;

    @NotBlank(message = "State is mandatory")
    @Size(max = 100, message = "State must be at most 100 characters")
    private String state;

    @NotBlank(message = "Zip Code is mandatory")
    @Pattern(regexp = "\\d{5,6}", message = "Zip Code must be 5 or 6 digits")
    private String zipCode;

    private AddressType addressType;  // Enum
}

