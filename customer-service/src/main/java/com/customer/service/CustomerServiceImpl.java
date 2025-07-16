package com.customer.service;

import com.customer.DTO.CustomerDTO;
import com.customer.constants.AppConstants;
import com.customer.entity.Address;
import com.customer.entity.AddressType;
import com.customer.entity.Customer;
import com.customer.exceptions.CustomerNotFoundException;
import com.customer.mapper.AddressMapper;
import com.customer.mapper.CustomerMapper;
import com.customer.repository.AddressRepository;
import com.customer.repository.CustomerRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO, boolean sameAddress) {
        Customer customer = customerMapper.toEntity(customerDTO);

        // Encrypt password
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        if (sameAddress) {
            List<Address> copied = copyAddresses(customer.getBillingAddress(), AddressType.SHIPPING);
            customer.setShippingAddress(copied);
        }

        assignCustomerToAddresses(customer);

        //  Set default role
//        customer.setRoles(Set.of("ROLE_CUSTOMER"));

        Customer saved = customerRepository.save(customer);
        return customerMapper.toDTO(saved);
    }

    @Override
    public CustomerDTO getCustomerById(Long customerId, String authenticatedEmail, Set<String> roles) {
        log.info("Finding customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        // ❌ Non-admins can only view their own data
        if (!roles.contains("ROLE_ADMIN") &&
                !customer.getEmailAddress().equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException("You are not authorized to view this customer's data.");
        }

        List<Address> allAddresses = addressRepository.findByCustomer(customer);

        customer.setBillingAddress(
                allAddresses.stream()
                        .filter(a -> a.getAddressType() == AddressType.BILLING)
                        .collect(Collectors.toList())
        );

        customer.setShippingAddress(
                allAddresses.stream()
                        .filter(a -> a.getAddressType() == AddressType.SHIPPING)
                        .collect(Collectors.toList())
        );

        return customerMapper.toDTO(customer);
    }



    @Override
    public List<CustomerDTO> getAllCustomers(String authenticatedEmail, Set<String> roles) {

        // ✅ Admins see all customers
        if (roles.contains("ROLE_ADMIN")) {
            List<Customer> customers = customerRepository.findAll();
            for (Customer customer : customers) {
                List<Address> allAddresses = addressRepository.findByCustomer(customer);

                customer.setBillingAddress(
                        allAddresses.stream()
                                .filter(a -> a.getAddressType() == AddressType.BILLING)
                                .collect(Collectors.toList())
                );

                customer.setShippingAddress(
                        allAddresses.stream()
                                .filter(a -> a.getAddressType() == AddressType.SHIPPING)
                                .collect(Collectors.toList())
                );
            }

            return customers.stream()
                    .map(customerMapper::toDTO)
                    .collect(Collectors.toList());
        }

        // ✅ Non-admins only get their own info
        Customer customer = customerRepository.findByEmailAddress(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found for email: " + authenticatedEmail));

        List<Address> allAddresses = addressRepository.findByCustomer(customer);

        customer.setBillingAddress(
                allAddresses.stream()
                        .filter(a -> a.getAddressType() == AddressType.BILLING)
                        .collect(Collectors.toList())
        );

        customer.setShippingAddress(
                allAddresses.stream()
                        .filter(a -> a.getAddressType() == AddressType.SHIPPING)
                        .collect(Collectors.toList())
        );

        return List.of(customerMapper.toDTO(customer));
    }


    @Override
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO updatedCustomerDTO, HttpServletRequest request) {

        Customer existing = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + customerId));

        // 🔒 Authorization check based on email
        String emailFromHeader = request.getHeader("X-Authenticated-Email");
        if (emailFromHeader == null || !emailFromHeader.equalsIgnoreCase(existing.getEmailAddress())) {
            throw new AccessDeniedException("You are not authorized to modify another customer's data.");
        }

        log.info("Updating customer with ID: {}", customerId);

        existing.setPhoneNumber(updatedCustomerDTO.getPhoneNumber());
        existing.setEmailAddress(updatedCustomerDTO.getEmailAddress());

        // Update and re-encrypt password
        if (updatedCustomerDTO.getNewPassword() != null && !updatedCustomerDTO.getNewPassword().isBlank()) {
            String newPassword = updatedCustomerDTO.getNewPassword();
            String retypePassword = updatedCustomerDTO.getRetypePassword();

            if (!newPassword.equals(retypePassword)) {
                throw new IllegalArgumentException(AppConstants.PASSWORD_MISMATCH);
            }

            if (!newPassword.matches(AppConstants.ALPHANUMERIC_CHARACTERS)) {
                throw new IllegalArgumentException(AppConstants.PASSWORD_FAIL_8_CHARACTERS);
            }

            if (passwordEncoder.matches(newPassword, existing.getPassword())) {
                throw new IllegalArgumentException(AppConstants.PASSWORD_OLD_MATCH);
            }

            existing.setPassword(passwordEncoder.encode(newPassword));
        }

        boolean sameAddress = updatedCustomerDTO.isSameAddress();
        List<Address> billingAddress = updatedCustomerDTO.getBillingAddress()
                .stream().map(addressMapper::toEntity).collect(Collectors.toList());

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

            shippingAddress = updatedCustomerDTO.getShippingAddress()
                    .stream().map(addressMapper::toEntity).collect(Collectors.toList());

            for (Address address : shippingAddress) {
                address.setAddressType(AddressType.SHIPPING);
                address.setCustomer(existing);
            }
        }

        existing.getBillingAddress().clear();
        existing.getBillingAddress().addAll(billingAddress);

        existing.getShippingAddress().clear();
        existing.getShippingAddress().addAll(shippingAddress);

        Customer updated = customerRepository.save(existing);
        return customerMapper.toDTO(updated);
    }



    @Override
    public void deleteCustomer(Long customerId, HttpServletRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + customerId));

        // 🔒 Authorization check
        String emailFromHeader = request.getHeader("X-Authenticated-Email");
        if (emailFromHeader == null || !emailFromHeader.equalsIgnoreCase(customer.getEmailAddress())) {
            throw new AccessDeniedException("You are not authorized to delete another customer's account.");
        }

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