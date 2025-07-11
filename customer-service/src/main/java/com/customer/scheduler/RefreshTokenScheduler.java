package com.customer.scheduler;

import com.customer.entity.RefreshToken;
import com.customer.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    // ⏱️ Runs every hour
    @Scheduled(fixedRate = 60 * 60 * 1000) // 1 hour in milliseconds
    public void purgeExpiredTokens() {
        Instant now = Instant.now();
        List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiredTrueOrExpiresAtBefore(now);

        if (!expiredTokens.isEmpty()) {
            log.info("🧹 Purging {} expired refresh tokens", expiredTokens.size());
            refreshTokenRepository.deleteAll(expiredTokens);
        } else {
            log.info("✅ No expired refresh tokens found at {}", now);
        }
    }
}

