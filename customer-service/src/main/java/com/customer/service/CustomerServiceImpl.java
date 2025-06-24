package com.customer.service;

import com.customer.entity.Address;
import com.customer.entity.AddressType;
import com.customer.entity.Customer;
import com.customer.exceptions.CustomerNotFoundException;
import com.customer.repository.AddressRepository;
import com.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public Customer createCustomer(Customer customer, boolean sameAddress) {

        log.info("Creating customer with details: {}", customer);
        if (sameAddress) {
            List<Address> copied = copyAddresses(customer.getBillingAddress(), AddressType.SHIPPING);
            customer.setShippingAddress(copied);
        }

        assignCustomerToAddresses(customer);

        log.info("Customer created with details: {}", customer);
        return customerRepository.save(customer);
    }

    @Override
    public Customer getCustomerById(Long customerId) {

        log.info("Finding customer with customer Id : {}", customerId);
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        List<Address> allAddresses = addressRepository.findByCustomer(customer);

        List<Address> billing = allAddresses.stream()
                .filter(a -> a.getAddressType() == AddressType.BILLING)
                .collect(Collectors.toList());

        List<Address> shipping = allAddresses.stream()
                .filter(a -> a.getAddressType() == AddressType.SHIPPING)
                .collect(Collectors.toList());

        customer.setBillingAddress(billing);
        customer.setShippingAddress(shipping);

        return customer;

    }

    @Override
    public List<Customer> getAllCustomers() {

        List<Customer> customers = customerRepository.findAll();

        for (Customer customer : customers) {
            List<Address> allAddresses = addressRepository.findByCustomer(customer);

            List<Address> billing = allAddresses.stream()
                    .filter(a -> a.getAddressType() == AddressType.BILLING)
                    .collect(Collectors.toList());

            List<Address> shipping = allAddresses.stream()
                    .filter(a -> a.getAddressType() == AddressType.SHIPPING)
                    .collect(Collectors.toList());

            customer.setBillingAddress(billing);
            customer.setShippingAddress(shipping);
        }

        return customers;
    }

    @Override
    public Customer updateCustomer(Long customerId, Customer updatedCustomer) {
        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        log.info("Updating customer with ID: {}", customerId);

        // Update phone number
        existing.setPhoneNumber(updatedCustomer.getPhoneNumber());

        // Handle sameAddress logic
        boolean sameAddress = updatedCustomer.isSameAddress();
        List<Address> billingAddress = updatedCustomer.getBillingAddress();
        List<Address> shippingAddress;

        if (sameAddress) {
            for (Address address : billingAddress) {
                address.setAddressType(AddressType.BILLING);
                address.setCustomer(existing);
            }

            shippingAddress = billingAddress.stream().map(b -> {
                Address copy = new Address();
                copy.setApartmentName(b.getApartmentName());
                copy.setAddLine1(b.getAddLine1());
                copy.setAddLine2(b.getAddLine2());
                copy.setCity(b.getCity());
                copy.setState(b.getState());
                copy.setZipCode(b.getZipCode());
                copy.setAddressType(AddressType.SHIPPING);
                copy.setCustomer(existing);
                return copy;
            }).collect(Collectors.toList());

        } else {
            for (Address address : billingAddress) {
                address.setAddressType(AddressType.BILLING);
                address.setCustomer(existing);
            }

            shippingAddress = updatedCustomer.getShippingAddress();
            for (Address address : shippingAddress) {
                address.setAddressType(AddressType.SHIPPING);
                address.setCustomer(existing);
            }
        }

        // Instead of setBillingAddress, modify the existing list
        existing.getBillingAddress().clear();
        existing.getBillingAddress().addAll(billingAddress);

        existing.getShippingAddress().clear();
        existing.getShippingAddress().addAll(shippingAddress);

        return customerRepository.save(existing);
    }


    @Override
    public void deleteCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));
        customerRepository.delete(customer);
    }

    private void assignCustomerToAddresses(Customer customer) {

        log.info(" Assigning address with customer Id : {}", customer.getCustomerId());
        customer.getBillingAddress().forEach(address -> {
            address.setCustomer(customer);
            address.setAddressType(AddressType.BILLING);
        });

        customer.getShippingAddress().forEach(address -> {
            address.setCustomer(customer);
            address.setAddressType(AddressType.SHIPPING);

            log.info(" Assigned address with customer Id : {}", customer.getCustomerId());
        });
    }

    private List<Address> copyAddresses(List<Address> original, AddressType type) {

        log.info(" Copying address with customer Id ");
        List<Address> copied = new ArrayList<>();
        for (Address address : original) {
            Address clone = new Address();
            clone.setApartmentName(address.getApartmentName());
            clone.setAddLine1(address.getAddLine1());
            clone.setAddLine2(address.getAddLine2());
            clone.setCity(address.getCity());
            clone.setState(address.getState());
            clone.setZipCode(address.getZipCode());
            clone.setAddressType(type);
            copied.add(clone);
        }

        log.info("Address Copied");
        return copied;
    }

}
