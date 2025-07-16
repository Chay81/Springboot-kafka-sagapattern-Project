package com.customer.controller;

import com.customer.DTO.CustomerDTO;
import com.customer.DTO.ForgotPasswordRequestDTO;
import com.customer.entity.CustomerResponse;
import com.customer.entity.PasswordResponse;
import com.customer.security.ForgotPasswordHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordHandler forgotPasswordHandler;

    @PatchMapping("/forgotPassword")
    public ResponseEntity<PasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequestDTO passwordRequestDTO) {
        PasswordResponse response = forgotPasswordHandler.resetPassword(passwordRequestDTO);

        if (response.isSuccess()) {
            log.info("✅ Password reset successful for: Email={}, Phone={}",
                    passwordRequestDTO.getEmail(), passwordRequestDTO.getPhoneNumber());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            log.warn("❌ Password reset failed: customer not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
