package com.customer.mapper;

import com.customer.DTO.AddressDTO;
import com.customer.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressDTO toDTO(Address address);

    Address toEntity(AddressDTO addressDTO);
}

