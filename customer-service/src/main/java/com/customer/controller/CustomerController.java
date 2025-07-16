package com.customer.controller;

import com.customer.DTO.CustomerDTO;
import com.customer.constants.AppConstants;
import com.customer.entity.CustomerResponse;
import com.customer.repository.CustomerRepository;
import com.customer.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    // Allowed roles (to avoid abuse)

    @PostMapping("/createCustomer")
    public ResponseEntity<CustomerResponse> createCustomer(@RequestBody CustomerDTO customerDTO) {

        // Check if both email and phone already exist together
        if (customerRepository.findByEmailAddressAndPhoneNumber(
                customerDTO.getEmailAddress(),
                customerDTO.getPhoneNumber()).isPresent()) {
            log.warn("❌ Account already exists for email {} and phone {}", customerDTO.getEmailAddress(), customerDTO.getPhoneNumber());
            CustomerResponse response = new CustomerResponse("An account already exists with this email address and phone number.", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Check if email already exists
        if (customerRepository.findByEmailAddress(customerDTO.getEmailAddress()).isPresent()) {
            log.warn("❌ Email already exists: {}", customerDTO.getEmailAddress());
            CustomerResponse response = new CustomerResponse("Email already exists.",null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // Check if phone number already exists
        if (customerRepository.findByPhoneNumber(customerDTO.getPhoneNumber()).isPresent()) {
            log.warn("❌ Phone number already exists: {}", customerDTO.getPhoneNumber());
            CustomerResponse response = new CustomerResponse("Phone number already exists.",null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        // 1. Determine roles
        List<String> requestedRoles = customerDTO.getRoles();
        Set<String> rolesToAssign = (requestedRoles == null || requestedRoles.isEmpty())
                ? Set.of("ROLE_CUSTOMER")
                : new HashSet<>(requestedRoles);

        for (String role : rolesToAssign) {
            if (!AppConstants.VALID_ROLES.contains(role)) {
                CustomerResponse response = new CustomerResponse("Invalid role: " + role ,null);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
                }
            }



        // Let the service handle the full customer saving
        customerDTO.setRoles(new ArrayList<>(rolesToAssign)); // send updated roles to service

        // 2. Create and persist customer
        log.info("Creating new customer : {}", customerDTO);
        CustomerDTO savedCustomer = customerService.createCustomer(customerDTO, customerDTO.isSameAddress());
        CustomerResponse response = new CustomerResponse("Customer created successfully",savedCustomer);
        log.info("Created new customer with details : {}", savedCustomer);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> getCustomerById(
            @PathVariable Long customerId,
            @RequestHeader("X-Authenticated-Email") String authenticatedEmail,
            @RequestHeader("X-Authenticated-Roles") String roleHeader
    ) {
        Set<String> roles = Arrays.stream(roleHeader.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        log.info("🔍 Getting customer by ID: {} for user: {}", customerId, authenticatedEmail);
        CustomerDTO existingCustomer = customerService.getCustomerById(customerId, authenticatedEmail, roles);
        return ResponseEntity.status(HttpStatus.OK).body(existingCustomer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers(
            @RequestHeader("X-Authenticated-Email") String authenticatedEmail,
            @RequestHeader("X-Authenticated-Roles") String roleHeader
    ) {
        Set<String> roles = Arrays.stream(roleHeader.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        log.info("📥 Getting customers for user: {}", authenticatedEmail);
        List<CustomerDTO> customers = customerService.getAllCustomers(authenticatedEmail, roles);
        return ResponseEntity.status(HttpStatus.OK).body(customers);
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerDTO customerDTO,
            HttpServletRequest request
    ) {
        log.info("Updating customer details : {}  with ID {}", customerDTO, customerId);
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerId, customerDTO, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCustomer);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId, HttpServletRequest request) {
        log.info("Deleting details of the customer with ID : {}", customerId);
        customerService.deleteCustomer(customerId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}