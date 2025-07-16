package com.customer.controller;

import com.customer.DTO.RefreshTokenFullRequestDTO;
import com.customer.entity.RefreshToken;
import com.customer.loginmodels.AuthRequest;
import com.customer.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {

        log.info("Inside login method of AuthController");
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(request));
        } catch (AuthenticationException ex) {
            log.info("Outside login method of AuthController");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid credentials"));
        }
    }

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(
            @RequestBody RefreshTokenFullRequestDTO request,
            @RequestHeader("X-Authenticated-Email") String authenticatedEmail) {

        try {
            log.info("Inside refreshToken method of AuthController");

            String refreshToken = request.getRefreshToken();
            AuthRequest authRequest = request.getAuthRequest();

            if (!authRequest.getEmail().equalsIgnoreCase(authenticatedEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Forbidden", "message", "Email mismatch"));
            }
            return ResponseEntity.ok(authService.refreshToken(refreshToken, authRequest));

        } catch (Exception e) {
            log.info("Leaving refreshToken method of AuthController");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", e.getMessage()));
        }
    }

    @PostMapping("/customer/logout")
    public ResponseEntity<?> logout(
            @RequestBody Map<String, String> request,
            @RequestHeader("X-Authenticated-Email") String authenticatedEmail) {

        log.info("Inside logout method of AuthController");
        String rawToken = request.get("refreshToken");
        if (rawToken == null || rawToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is required for logout.");
        }

        Optional<RefreshToken> token = authService.findByRawToken(rawToken);
        if (token.isEmpty() || !token.get().getUsername().equalsIgnoreCase(authenticatedEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("You are not authorized to logout this token.");
        }

        authService.logout(rawToken);
        log.info("Leaving logout method of AuthController");
        return ResponseEntity.ok("Logout successful");
    }
}