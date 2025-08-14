package com.customer.config;

import com.customer.repository.CustomerPasswordsRepository;
import com.customer.repository.CustomerRepository;
import com.customer.util.PasswordValidatorUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PasswordValidatorInitializer {

    private final CustomerRepository customerRepository;
    private final CustomerPasswordsRepository historyRepository;

    @PostConstruct
    public void initPasswordValidator() {
        PasswordValidatorUtil.init(customerRepository, historyRepository);
    }
}

