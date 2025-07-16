package com.customer.controller;

import com.customer.entity.RefreshToken;
import com.customer.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/tokens")
public class AdminTokenAuditController {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Lists all expired refresh tokens.
     * Only accessible by users with ROLE_ADMIN.
     */
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getExpiredTokens() {
        List<RefreshToken> expiredTokens = refreshTokenRepository
                .findByExpiredTrueOrExpiresAtBefore(Instant.now());

        return ResponseEntity.ok(Map.of(
                "count", expiredTokens.size(),
                "data", expiredTokens
        ));
    }
}


