package com.customer.security;

import com.customer.DTO.ForgotPasswordRequestDTO;
import com.customer.entity.Customer;
import com.customer.entity.PasswordResponse;
import com.customer.repository.CustomerRepository;
import com.customer.util.ForgotPasswordValidator;
import com.customer.util.PasswordValidatorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordHandler {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ForgotPasswordValidator forgotPasswordValidator;

    public PasswordResponse resetPassword(ForgotPasswordRequestDTO requestDTO) {

        log.info("Entering resetPassword method");
        // Step 1: Validate identity and fetch customer
        Customer customer = forgotPasswordValidator.validateAndFetchCustomer(requestDTO);

        // Step 2: Validating password requirements
        PasswordResponse response = PasswordValidatorUtil
                .validateResetPassword(requestDTO, customer, passwordEncoder);

        if (!response.isSuccess()) {
            return response;
        }

        log.info("End of resetPassword method");
        return response;

    }
}

