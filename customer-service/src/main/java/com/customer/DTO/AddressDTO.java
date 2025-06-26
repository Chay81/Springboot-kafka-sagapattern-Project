package com.customer.DTO;

import com.customer.entity.AddressType;
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

    private String apartmentName;
    private String addLine1;
    private String addLine2;
    private String city;
    private String state;
    private String zipCode;

    private AddressType addressType;  // Enum
}

