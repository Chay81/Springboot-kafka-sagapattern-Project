package com.customer.mapper;

import com.customer.DTO.CustomerDTO;
import com.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface CustomerMapper {

    @Mapping(source = "customerId", target = "customerId", qualifiedByName = "encodeCustomerId")
    CustomerDTO toDTO(Customer customer);

    @Mapping(target = "customerId", ignore = true) // Let DB generate it
    Customer toEntity(CustomerDTO dto);

    // You can write this directly in Mapper or in a helper class
    @Named("encodeCustomerId")
    default String encodeCustomerId(Long id) {
        if (id == null) return null;
        return java.util.UUID.nameUUIDFromBytes(("customer-" + id).getBytes()).toString();
    }
}
