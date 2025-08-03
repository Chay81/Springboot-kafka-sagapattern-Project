package com.customer.mapper;

import com.customer.DTO.AddressDTO;
import com.customer.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    @Mapping(target = "customer", ignore = true)
    Address toEntity(AddressDTO dto);

    AddressDTO toDTO(Address address);

}

