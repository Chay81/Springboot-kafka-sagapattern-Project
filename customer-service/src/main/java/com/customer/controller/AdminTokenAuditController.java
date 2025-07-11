package com.customer.controller;

import com.customer.entity.RefreshToken;
import com.customer.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/tokens")
@RequiredArgsConstructor
public class AdminTokenAuditController {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Lists all expired refresh tokens.
     * Restrict this endpoint via JWT role (only ROLE_ADMIN should access).
     */

    @GetMapping("/expired")
    public ResponseEntity<?> getExpiredTokens() {
        List<RefreshToken> expiredTokens = refreshTokenRepository
                .findByExpiredTrueOrExpiresAtBefore(Instant.now());

        return ResponseEntity.ok(Map.of(
                "count", expiredTokens.size(),
                "data", expiredTokens
        ));
    }
}

