package com.customer.controller;

import com.customer.DTO.CustomerDTO;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerDTO> createCustomer(@RequestBody CustomerDTO customerDTO) {

        log.info("Creating new customer : {}", customerDTO);
        CustomerDTO savedCustomer = customerService.createCustomer(customerDTO, customerDTO.isSameAddress());

        log.info("Created new customer with details : {}", savedCustomer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }


    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long customerId) {

        log.info("Details of the customer with ID : {}", customerId);
        CustomerDTO existingCustomer = customerService.getCustomerById(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(existingCustomer);
    }

    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {

        log.info("Details of all the customers:");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> updateCustomer(
            @PathVariable Long customerId,
            @RequestBody CustomerDTO customerDTO
    ) {
        log.info("Updating customer details : {}  with ID {}", customerDTO, customerId);
        CustomerDTO updatedCustomer = customerService.updateCustomer(customerId, customerDTO);
        return ResponseEntity.status(HttpStatus.OK).body(updatedCustomer);
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long customerId) {

        log.info("Deleting details of the customer with ID : {}", customerId);
        customerService.deleteCustomer(customerId);  // Throws exception if not found
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

}