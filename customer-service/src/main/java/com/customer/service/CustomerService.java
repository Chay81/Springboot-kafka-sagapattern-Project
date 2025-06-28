package com.customer.service;

import com.customer.DTO.CustomerDTO;

import java.util.List;

public interface CustomerService {

    CustomerDTO createCustomer(CustomerDTO customerDTO, boolean sameAddress);

    CustomerDTO getCustomerById(Long customerId);

    List<CustomerDTO> getAllCustomers();

    CustomerDTO updateCustomer(Long customerId, CustomerDTO updatedCustomerDTO);

    void deleteCustomer(Long customerId);
}

