package com.customer.security;

import com.customer.DTO.ForgotPasswordRequestDTO;
import com.customer.constants.AppConstants;
import com.customer.entity.Customer;
import com.customer.entity.PasswordResponse;
import com.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ForgotPasswordHandler {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public PasswordResponse resetPassword(ForgotPasswordRequestDTO requestDTO) {
        Optional<Customer> optionalCustomer = Optional.empty();

        // Validate matching newPassword and retypePassword
        if (!requestDTO.getNewPassword().equals(requestDTO.getRetypePassword())) {
            return new PasswordResponse(false, AppConstants.PASSWORD_MISMATCH);
        }

        // Validate password strength (at least 8 alphanumeric characters)
        if (!requestDTO.getNewPassword().matches(AppConstants.ALPHANUMERIC_CHARACTERS)) {
            return new PasswordResponse(false, AppConstants.PASSWORD_FAIL_8_CHARACTERS);
        }

        // Validate customer by phoneNumner or emailAddress
        if (requestDTO.getEmail() != null && !requestDTO.getEmail().isEmpty()) {
            optionalCustomer = customerRepository.findByEmailAddress(requestDTO.getEmail());
        } else if (requestDTO.getPhoneNumber() != null && !requestDTO.getPhoneNumber().isEmpty()) {
            optionalCustomer = customerRepository.findByPhoneNumber(requestDTO.getPhoneNumber());
        }

        if (optionalCustomer.isPresent()) {
            Customer customer = optionalCustomer.get();

            // Check if new password is same as old
            if (passwordEncoder.matches(requestDTO.getNewPassword(), customer.getPassword())) {
                return new PasswordResponse(false, AppConstants.PASSWORD_OLD_MATCH);
            }

            customer.setPassword(passwordEncoder.encode(requestDTO.getNewPassword()));
            customerRepository.save(customer);

            return new PasswordResponse(true, AppConstants.PASSWORD_SUCCESS);
        }

        return new PasswordResponse(false, AppConstants.CUSTOMER_NOT_FOUND);
    }
}

