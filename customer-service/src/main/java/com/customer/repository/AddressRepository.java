package com.customer.repository;

import com.customer.entity.Address;
import com.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomer(Customer customer);
}
