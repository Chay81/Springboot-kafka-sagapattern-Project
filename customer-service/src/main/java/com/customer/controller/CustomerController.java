package com.customer.controller;

import com.customer.entity.Customer;
import com.customer.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.ResourceNotFoundException;
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
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {

        log.info("Creating new customer : {}", customer);
        Customer savedCustomer = customerService.createCustomer(customer);

        log.info("Created new customer with details : {}", customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedCustomer);
    }


    @PutMapping("/{customerId}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long customerId, @RequestBody Customer customer) {

        log.info("Updating customer details : {}  with ID {}", customer, customerId);
        Customer existingCustomer = customerService.updateCustomer(customerId, customer);
        return ResponseEntity.status(HttpStatus.OK).body(existingCustomer);
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable Long customerId) {

        log.info("Details of the customer with ID : {}", customerId);
        Customer existingCustomer = customerService.getCustomerById(customerId);
        return ResponseEntity.status(HttpStatus.OK).body(existingCustomer);
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {

        log.info("Details of all the customers:");
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @DeleteMapping("/{customerId}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long customerId) {
    log.info("Deleting details of the customer with ID : {}", customerId);
    customerService.deleteCustomer(customerId);  // Throws exception if not found
    return ResponseEntity.ok("Customer with ID " + customerId + " deleted successfully");
}
	

}
