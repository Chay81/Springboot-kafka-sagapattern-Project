//package com.customer.security;
//
//import com.customer.entity.Customer;
//import com.customer.repository.CustomerRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.core.userdetails.UsernameNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.Collections;
//
//@Service
//public class CustomerUserDetailsService implements UserDetailsService {
//
//    @Autowired
//    private CustomerRepository customerRepository;
//
//    @Override
//    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//        Customer customer = customerRepository.findByEmailAddress(email)
//                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));
//
//        // Convert Customer to Spring Security UserDetails
//        return new User(
//                customer.getEmailAddress(),
//                customer.getPassword(),
//                    Collections.emptyList() // no roles/authorities for now
//        );
//    }
//}
