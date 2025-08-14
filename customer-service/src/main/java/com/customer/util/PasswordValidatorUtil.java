package com.customer.util;

import com.customer.DTO.ForgotPasswordRequestDTO;
import com.customer.constants.AppConstants;
import com.customer.entity.Customer;
import com.customer.entity.CustomerPasswords;
import com.customer.entity.PasswordResponse;
import com.customer.repository.CustomerPasswordsRepository;
import com.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


@Slf4j
@UtilityClass
public class PasswordValidatorUtil {

    private static CustomerRepository customerRepository;
    private static CustomerPasswordsRepository historyRepository;
    private static boolean initialized = false;

    // Called once during app startup by the initializer
    public void init(CustomerRepository repo, CustomerPasswordsRepository historyRepo) {
        customerRepository = Objects.requireNonNull(repo, "CustomerRepository cannot be null");
        historyRepository = Objects.requireNonNull(historyRepo, "CustomerPasswordsRepository cannot be null");
        initialized = true;
        log.info("✅ PasswordValidatorUtil initialized.");
    }


    /**
     * Enforces:
     * - new/retype match
     * - strength policy
     * - not equal to current
     * - not equal to any of the last 7 old passwords
     * Then:
     * - saves old current password into history
     * - updates customer password
     * - trims history to last 7
     */

    public void validateNewAndRetypePasswords(String password, String retypePassword) {

        assertInitialized();
        
        if (password == null || retypePassword == null || password.isBlank() || retypePassword.isBlank()) {
            throw new IllegalArgumentException(AppConstants.PASSWORD_REQUIRED);
        }

        // Step 1: Validate password fields
        if (!password.equals(retypePassword)) {
            throw new IllegalArgumentException(AppConstants.PASSWORD_MISMATCH);
        }

        if (!password.matches(AppConstants.ALPHANUMERIC_CHARACTERS_STRONG_PASSWORD)) {
            throw new IllegalArgumentException(AppConstants.PASSWORD_FAIL_8_CHARACTERS);
        }
    }

    @Transactional
    public void validateAndUpdatePassword(Customer customer, String newPassword, PasswordEncoder passwordEncoder) {

        assertInitialized();

        // 1) Compare against current password
        if (passwordEncoder.matches(newPassword, customer.getPassword())) {
            throw new IllegalArgumentException(AppConstants.PASSWORD_OLD_MATCH);
        }

        // 2) Compare against last 7 old passwords (newest first)
        List<CustomerPasswords> lastSevenPasswords =
                historyRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(
                        customer.getCustomerId(), PageRequest.of(0, 7));

        boolean recentlyUsed = lastSevenPasswords.stream()
                .anyMatch(p -> passwordEncoder.matches(newPassword, p.getEncodedPassword()));

        if (recentlyUsed) {
            throw new IllegalArgumentException(AppConstants.PASSWORD_RECENTLY_USED);
        }

        // 3) Persist the current password into history
        historyRepository.save(
                CustomerPasswords.builder()
                        .customer(customer)
                        .encodedPassword(customer.getPassword())
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 4) Update to the new password
        String encodedNew = passwordEncoder.encode(newPassword);
        customer.setPassword(encodedNew);

        // 5) Trim old history
        trimOldPasswords(customer.getCustomerId());
    }


    @Transactional
    public PasswordResponse validateResetPassword(ForgotPasswordRequestDTO dto, Customer customer, PasswordEncoder encoder) {

        assertInitialized();
        String newPassword = dto.getNewPassword();
        String retypePassword = dto.getRetypePassword();

        // 1) Match check
        if (!newPassword.equals(retypePassword)) {
            return new PasswordResponse(false, AppConstants.PASSWORD_MISMATCH);
        }

        // 2) Strength check
        if (!newPassword.matches(AppConstants.ALPHANUMERIC_CHARACTERS_STRONG_PASSWORD)) {
            return new PasswordResponse(false, AppConstants.PASSWORD_FAIL_8_CHARACTERS);
        }

        // 3) Compare against current password
        if (encoder.matches(newPassword, customer.getPassword())) {
            return new PasswordResponse(false, AppConstants.PASSWORD_OLD_MATCH);
        }

        // 4) Compare against last 7 old passwords (newest first)
        List<CustomerPasswords> lastSevenPasswords =
                historyRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(
                        customer.getCustomerId(), PageRequest.of(0, 7));

        boolean recentlyUsed = lastSevenPasswords.stream()
                .anyMatch(p -> encoder.matches(newPassword, p.getEncodedPassword()));

        if (recentlyUsed) {
            return new PasswordResponse(false, AppConstants.PASSWORD_RECENTLY_USED);
        }

        // 5) Persist the previous (current) password into history
        historyRepository.save(
                CustomerPasswords.builder()
                        .customer(customer)
                        .encodedPassword(customer.getPassword()) // setting the encodedPassword in the CustomerPassword class
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 6) Update to the new password
        String encodedNew = encoder.encode(newPassword);
        customer.setPassword(encodedNew);
        customerRepository.save(customer);

        // 7) Trim history
        trimOldPasswords(customer.getCustomerId());

        log.info(AppConstants.PASSWORDS_TRIMMED, customer.getCustomerId());
        return new PasswordResponse(true, AppConstants.PASSWORD_SUCCESS);

    }

    private static void assertInitialized() {
        if (!initialized) {
            throw new IllegalStateException(AppConstants.PASSWORD_VALIDATOR_UTIL_INITIALIZED);
        }
    }

    private void trimOldPasswords(Long customerId) {

        // 1) Keep only last 7 entries in history
        List<CustomerPasswords> allPasswords =
                historyRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId);

        // Extra safety: sort explicitly by createdAt DESC in-memory before trimming
        allPasswords.sort(Comparator.comparing(CustomerPasswords::getCreatedAt).reversed());

        if (allPasswords.size() > 7) {
            List<CustomerPasswords> toDelete = new ArrayList<>(allPasswords.subList(7, allPasswords.size()));
            historyRepository.deleteAll(toDelete);
            log.info(AppConstants.DELETED_PASSWORDS, toDelete.size(), customerId);
        }
    }
}

