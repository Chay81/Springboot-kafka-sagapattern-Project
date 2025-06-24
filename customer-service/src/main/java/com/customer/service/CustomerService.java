package com.customer.service;

import com.customer.entity.Customer;

import java.util.List;

public interface CustomerService {

    Customer createCustomer(Customer customer, boolean sameAddress);
    Customer getCustomerById(Long customerId);
    List<Customer> getAllCustomers();
    public Customer updateCustomer(Long customerId, Customer updatedCustomer);
    void deleteCustomer(Long customerId);
}
