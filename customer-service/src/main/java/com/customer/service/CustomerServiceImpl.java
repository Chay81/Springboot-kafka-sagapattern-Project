package com.customer.service;

import com.customer.entity.Address;
import com.customer.entity.Customer;
import com.customer.exceptions.CustomerNotFoundException;
import com.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    public Customer createCustomer(Customer customer) {
        log.info("Creating customer with details: {}", customer);

        List<Address> finalAddresses = new ArrayList<>();

        for (Address addr : customer.getAddresses()) {
            if ("BILLING".equalsIgnoreCase(addr.getAddressType())) {
                addr.setCustomer(customer);
                finalAddresses.add(addr);

                // If sameAddress is true, clone this address as SHIPPING
                if (customer.isSameAddress()) {
                    Address shippingCopy = new Address();
                    BeanUtils.copyProperties(addr, shippingCopy, "addressId");
                    shippingCopy.setAddressType("SHIPPING");
                    shippingCopy.setCustomer(customer);
                    finalAddresses.add(shippingCopy);
                }
            } else if ("SHIPPING".equalsIgnoreCase(addr.getAddressType()) && !customer.isSameAddress()) {
                addr.setCustomer(customer);
                finalAddresses.add(addr);
            }
        }

        customer.setAddresses(finalAddresses);
        return customerRepository.save(customer);
    }

    @Override
    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        log.info("Updating customer with ID: {}", id);

        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));

        existing.setCustomerName(updatedCustomer.getCustomerName());
        existing.setPhoneNumber(updatedCustomer.getPhoneNumber());
        existing.setSameAddress(updatedCustomer.isSameAddress());

        List<Address> updatedAddresses = new ArrayList<>();

        for (Address addr : updatedCustomer.getAddresses()) {
            if ("BILLING".equalsIgnoreCase(addr.getAddressType())) {
                addr.setCustomer(existing);
                updatedAddresses.add(addr);

                if (updatedCustomer.isSameAddress()) {
                    Address shippingCopy = new Address();
                    BeanUtils.copyProperties(addr, shippingCopy, "addressId");
                    shippingCopy.setAddressType("SHIPPING");
                    shippingCopy.setCustomer(existing);
                    updatedAddresses.add(shippingCopy);
                }

            } else if ("SHIPPING".equalsIgnoreCase(addr.getAddressType()) && !updatedCustomer.isSameAddress()) {
                addr.setCustomer(existing);
                updatedAddresses.add(addr);
            }
        }

        existing.getAddresses().clear(); // Clean old addresses
        existing.getAddresses().addAll(updatedAddresses); // Replace with updated ones

        log.info("Updated customer saved: {}", existing);
        return customerRepository.save(existing);
    }


    @Override
    public Customer getCustomerById(Long customerId) {

        log.info("Finding customer with customer Id : {}", customerId);
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        customerRepository.delete(customer);
    }
}
