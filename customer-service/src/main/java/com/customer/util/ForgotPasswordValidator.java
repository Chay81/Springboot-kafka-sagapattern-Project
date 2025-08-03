package com.customer.util;

import com.customer.DTO.ForgotPasswordRequestDTO;
import com.customer.constants.AppConstants;
import com.customer.entity.Customer;
import com.customer.exceptions.CustomerNotFoundException;
import com.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ForgotPasswordValidator {

    private final CustomerRepository customerRepository;

    public Customer validateAndFetchCustomer(ForgotPasswordRequestDTO requestDTO) {
        boolean hasEmail = requestDTO.getEmail() != null && !requestDTO.getEmail().isEmpty();
        boolean hasPhone = requestDTO.getPhoneNumber() != null && !requestDTO.getPhoneNumber().toString().isEmpty();

            //  Both email and phoneNumber are provided (non-null and non-empty)
            //  CustomerNotFoundException: if no customer found by the email.
            //  IllegalArgumentException: if phone doesn't match DB value for that email.
        if (hasEmail && hasPhone) {
            Optional<Customer> optionalCustomer = customerRepository.findByEmailAddress(requestDTO.getEmail());

            if (optionalCustomer.isEmpty()) {
                throw new CustomerNotFoundException(AppConstants.CUSTOMER_NOT_FOUND);
            }

            Customer customer = optionalCustomer.get();

            if (!requestDTO.getPhoneNumber().equals(customer.getPhoneNumber())) {
                throw new IllegalArgumentException(AppConstants.PHONE_NOT_FOUND);
            }

            return customer;

            //  email is provided but phoneNumber is missing (null or empty string)
            //  CustomerNotFoundException: if no customer found by the given email.
        } else if (hasEmail) {
            return customerRepository.findByEmailAddress(requestDTO.getEmail())
                    .orElseThrow(() -> new CustomerNotFoundException(AppConstants.CUSTOMER_NOT_FOUND));

            //  phoneNumber is provided but email is missing (null or empty string)
            //  CustomerNotFoundException: if no customer found by the given phone number.
        } else if (hasPhone) {
            return customerRepository.findByPhoneNumber(requestDTO.getPhoneNumber())
                    .orElseThrow(() -> new CustomerNotFoundException(AppConstants.CUSTOMER_NOT_FOUND));

            //  Neither email nor phoneNumber is provided (both are null or empty)
            //  IllegalArgumentException: with message PROVIDE_EMAIL_OR_PHONE
        } else {
            throw new IllegalArgumentException(AppConstants.PROVIDE_EMAIL_OR_PHONE);
        }
    }
}
