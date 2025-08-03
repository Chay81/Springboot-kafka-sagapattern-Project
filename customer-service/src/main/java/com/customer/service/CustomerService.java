package com.customer.service;

import com.customer.DTO.CustomerDTO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Set;

public interface CustomerService {

    CustomerDTO createCustomer(CustomerDTO customerDTO, boolean sameAddress);

    CustomerDTO getCustomerById(Long customerId, String authenticatedEmail, Set<String> roles);

    List<CustomerDTO> getAllCustomers(String authenticatedEmail, Set<String> roles);

    CustomerDTO updateCustomer(Long customerId, CustomerDTO updatedCustomerDTO, String authenticatedEmail);

    void deleteCustomer(Long customerId, String authenticatedEmail);

// the changes for UpdateCustomer and delete customer with parameters, HttpServletRequest are due to below reasons
// Prevent users from updating or deleting other customers’ data by ensuring that:
//  A customer can only update/delete their own account.
// This is validated using the X-Authenticated-Email header set by the Gateway, which decodes the JWT and injects the email.
}

