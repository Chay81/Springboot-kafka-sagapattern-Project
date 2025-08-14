package com.customer.repository;

import com.customer.entity.CustomerPasswords;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerPasswordsRepository  extends JpaRepository<CustomerPasswords, Long> {

    // All history (newest first)
    List<CustomerPasswords> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId);

    // Top N history (newest first)
    List<CustomerPasswords> findByCustomer_CustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

}
