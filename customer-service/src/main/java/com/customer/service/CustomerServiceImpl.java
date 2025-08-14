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
import com.customer.util.DataMaskingUtil;
import com.customer.util.PasswordValidatorUtil;
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

        validatePasswordForCreation(customerDTO); // Added validation step for password validation
        Customer customer = customerMapper.toEntity(customerDTO);

        // Encrypt password after validation
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        if (sameAddress) {
            List<Address> copied = copyAddress(customer.getBillingAddress(), AddressType.SHIPPING);
            customer.setShippingAddress(copied);
        }

        assignCustomerToAddress(customer);

        Customer saved = customerRepository.save(customer);
//      Masking emailAddress and phoneNumber in api response
//      return customerMapper.toDTO(saved);
        return maskSensitiveData(customerMapper.toDTO(saved));

    }

    @Override
    public CustomerDTO getCustomerById(Long customerId, String authenticatedEmail, Set<String> roles) {
        log.info("Finding customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CUSTOMER_NOT_AVAILABLE+ customerId));

        // ❌ Non-admins can only view their own data
        if (!roles.contains("ROLE_ADMIN") &&
                !customer.getEmailAddress().equalsIgnoreCase(authenticatedEmail)) {
            throw new AccessDeniedException(AppConstants.VIEW_CUSTOMER);
        }

        hydrateAddresses(customer);
        return maskSensitiveData(customerMapper.toDTO(customer));
    }

    @Override
    public List<CustomerDTO> getAllCustomers(String authenticatedEmail, Set<String> roles) {
        // ✅ Admins see all customers
        if (roles.contains("ROLE_ADMIN")) {
            List<Customer> customers = customerRepository.findAll();
            customers.forEach(this::hydrateAddresses);

            return customers.stream()
                    .map(customerMapper::toDTO)
                    .collect(Collectors.toList());

        }

        // ✅ Non-admins only get their own info
        Customer customer = customerRepository.findByEmailAddress(authenticatedEmail)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CUSTOMER_NOT_FOUND + authenticatedEmail));

        hydrateAddresses(customer);
        return List.of(maskSensitiveData(customerMapper.toDTO(customer)));
    }

    @Override
    public CustomerDTO updateCustomer(Long customerId, CustomerDTO updatedCustomerDTO, String authenticatedEmail) {

        Customer existingDetails = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CUSTOMER_NOT_AVAILABLE + customerId));

        // 🔒 Authorization check based on email
        if (authenticatedEmail == null || !authenticatedEmail.equalsIgnoreCase(existingDetails.getEmailAddress())) {
            throw new AccessDeniedException(AppConstants.MODIFY_CUSTOMER);
        }

        log.info("Updating customer with ID: {}", customerId);

        existingDetails.setPhoneNumber(updatedCustomerDTO.getPhoneNumber());
        existingDetails.setEmailAddress(updatedCustomerDTO.getEmailAddress());

        handlePasswordUpdate(existingDetails, updatedCustomerDTO);

        boolean sameAddress = updatedCustomerDTO.isSameAddress();

        List<Address> billingAddress = updatedCustomerDTO.getBillingAddress()
                .stream().map(addressMapper::toEntity).collect(Collectors.toList());

        List<Address> shippingAddress;

        if (sameAddress) {
            for (Address address : billingAddress) {
                address.setAddressType(AddressType.BILLING);
                address.setCustomer(existingDetails);
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
                copy.setCustomer(existingDetails);
                return copy;
            }).collect(Collectors.toList());

        } else {
            for (Address address : billingAddress) {
                address.setAddressType(AddressType.BILLING);
                address.setCustomer(existingDetails);
            }

            shippingAddress = updatedCustomerDTO.getShippingAddress()
                    .stream().map(addressMapper::toEntity).collect(Collectors.toList());

            for (Address address : shippingAddress) {
                address.setAddressType(AddressType.SHIPPING);
                address.setCustomer(existingDetails);
            }
        }

        existingDetails.getBillingAddress().clear();
        existingDetails.getBillingAddress().addAll(billingAddress);

        existingDetails.getShippingAddress().clear();
        existingDetails.getShippingAddress().addAll(shippingAddress);

        Customer updated = customerRepository.save(existingDetails);

        return maskSensitiveData(customerMapper.toDTO(updated));
    }



    @Override
    public void deleteCustomer(Long customerId, String authenticatedEmail) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(AppConstants.CUSTOMER_NOT_AVAILABLE+ customerId));

        // 🔒 Authorization check
        if (authenticatedEmail == null || !authenticatedEmail.equalsIgnoreCase(customer.getEmailAddress())) {
            throw new AccessDeniedException(AppConstants.DELETE_CUSTOMER);
        }

        customerRepository.delete(customer);
    }

    private void assignCustomerToAddress(Customer customer) {

        log.info("Assigning address with customer Id : {}", customer.getCustomerId());
        customer.getBillingAddress().forEach(address -> {
            address.setCustomer(customer);
            address.setAddressType(AddressType.BILLING);
        });

        customer.getShippingAddress().forEach(address -> {
            address.setCustomer(customer);
            address.setAddressType(AddressType.SHIPPING);

            log.info("Assigned address with customer Id : {}", customer.getCustomerId());
        });
    }

    private List<Address> copyAddress(List<Address> original, AddressType type) {

        log.info("Copying address with customer Id ");
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

    // 🔄 Utility to hydrate billing and shipping addresses
    private void hydrateAddresses(Customer customer) {
        List<Address> all = addressRepository.findByCustomer(customer);
        customer.setBillingAddress(filterAddresses(all, AddressType.BILLING));
        customer.setShippingAddress(filterAddresses(all, AddressType.SHIPPING));
    }

    private List<Address> filterAddresses(List<Address> addresses, AddressType type) {
        return addresses.stream()
                .filter(a -> a.getAddressType() == type)
                .collect(Collectors.toList());
    }

    private void handlePasswordUpdate(Customer existing, CustomerDTO dto) {

        log.info("Entering handlePasswordUpdate method");
        if (dto.getNewPassword() == null || dto.getNewPassword().isBlank()) return;

        PasswordValidatorUtil.validateNewAndRetypePasswords(dto.getNewPassword(), dto.getRetypePassword());
        PasswordValidatorUtil.validateAndUpdatePassword(existing, dto.getNewPassword(), passwordEncoder);

        log.info("End of handlePasswordUpdate method");
    }

    private void validatePasswordForCreation(CustomerDTO dto) {

        log.info("Entering validatePasswordForCreation method");
        PasswordValidatorUtil.validateNewAndRetypePasswords(dto.getPassword(), dto.getRetypePassword());
        log.info("End of validatePasswordForCreation method");
    }

    // ✅ Central masking utility
    private CustomerDTO maskSensitiveData(CustomerDTO dto) {
        dto.setEmailAddress(DataMaskingUtil.maskEmail(dto.getEmailAddress()));
        dto.setPhoneNumber(DataMaskingUtil.maskPhone(dto.getPhoneNumber()));
        return dto;
    }
}