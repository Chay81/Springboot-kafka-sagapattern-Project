package com.customer.util;

import com.customer.DTO.ForgotPasswordRequestDTO;
import com.customer.entity.PasswordResponse;
import com.customer.entity.Customer;
import com.customer.constants.AppConstants;
import lombok.experimental.UtilityClass;
import org.springframework.security.crypto.password.PasswordEncoder;

@UtilityClass
public class PasswordValidatorUtil {

    public void validateNewAndRetypePasswords(String password, String retypePassword) {
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

    public void validateAndUpdatePassword(Customer customer, String newPassword, PasswordEncoder passwordEncoder) {
        if (passwordEncoder.matches(newPassword, customer.getPassword())) {
            throw new IllegalArgumentException(AppConstants.PASSWORD_OLD_MATCH);
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
    }

    public PasswordResponse validateResetPassword(ForgotPasswordRequestDTO dto, Customer customer, PasswordEncoder encoder) {
        String newPassword = dto.getNewPassword();
        String retypePassword = dto.getRetypePassword();

        if (!newPassword.equals(retypePassword)) {
            return new PasswordResponse(false, AppConstants.PASSWORD_MISMATCH);
        }

        if (!newPassword.matches(AppConstants.ALPHANUMERIC_CHARACTERS_STRONG_PASSWORD)) {
            return new PasswordResponse(false, AppConstants.PASSWORD_FAIL_8_CHARACTERS);
        }

        if (encoder.matches(newPassword, customer.getPassword())) {
            return new PasswordResponse(false, AppConstants.PASSWORD_OLD_MATCH);
        }

        customer.setPassword(encoder.encode(newPassword));
        return new PasswordResponse(true, AppConstants.PASSWORD_SUCCESS);
    }
}

