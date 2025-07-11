package com.customer.repository;

import com.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmailAddress(String emailAddress);

    Optional<Customer> findByPhoneNumber(String phoneNumber);

    Optional<Customer> findByEmailAddressAndPhoneNumber(String emailAddress, String phoneNumber);
}

