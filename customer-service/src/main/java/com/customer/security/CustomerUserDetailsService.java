package com.customer.security;

import com.customer.entity.Customer;
import com.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Customer customer = customerRepository.findByEmailAddress(email)
                .orElseThrow(() -> new UsernameNotFoundException("Customer not found with email: " + email));

        Set<GrantedAuthority> authorities = customer.getRoles()
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        log.info(" Roles for user {}: {}", customer.getEmailAddress(), customer.getRoles());
        // Convert Customer to Spring Security UserDetails
        return new User(
                customer.getEmailAddress(),
                customer.getPassword(),
                authorities
        );
    }
}
