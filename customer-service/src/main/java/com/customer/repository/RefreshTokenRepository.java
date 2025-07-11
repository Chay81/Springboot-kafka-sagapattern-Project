package com.customer.repository;

import com.customer.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    Optional<RefreshToken> findByRawToken(String rawToken);
    void deleteByUsername(String username);
    List<RefreshToken> findByExpiredTrueOrExpiresAtBefore(Instant now);

}

