package com.customer.service;

import com.customer.entity.RefreshToken;
import com.customer.loginmodels.AuthRequest;
import com.customer.repository.RefreshTokenRepository;
import com.customer.util.RSAEncryptor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RSAEncryptor rsaEncryptor;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private static final long REFRESH_TOKEN_EXPIRY_MS = 30 * 60 * 1000;

    public Map<String, String> login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(HashSet::new));

        long now = System.currentTimeMillis();
        long expiryTime = now + jwtExpirationInMs;

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("emailAddress", userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String rawRefreshToken = UUID.randomUUID().toString();
        String encryptedToken = rsaEncryptor.encrypt(rawRefreshToken);

        refreshTokenRepository.deleteByUsername(userDetails.getUsername()); // clear old

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .token(encryptedToken)
                .rawToken(rawRefreshToken)
                .username(userDetails.getUsername())
                .roles(roles)
                .issuedAt(Instant.ofEpochMilli(now))
                .expiresAt(Instant.ofEpochMilli(now + REFRESH_TOKEN_EXPIRY_MS))
                .expired(false)
                .build();

        refreshTokenRepository.save(refreshTokenEntity);

        return Map.of(
                "message", "Login successful",
                "token", token,
                "expiresAt", Instant.ofEpochMilli(expiryTime).toString(),
                "refreshToken", rawRefreshToken
        );
    }

    public Map<String, String> refreshToken(
            String rawToken, AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByRawToken(rawToken);

        if (optionalToken.isEmpty()) {
            throw new RuntimeException("Invalid refresh token");
        }

        RefreshToken token = optionalToken.get();

        // 🔐 Ownership validation: ensure token belongs to the authenticated user
        if (!token.getUsername().equals(userDetails.getUsername())) {
            throw new RuntimeException("Refresh token does not belong to this user");
        }

        if (token.isExpired() || token.getExpiresAt().isBefore(Instant.now())) {
            token.setExpired(true);
            refreshTokenRepository.save(token);
            throw new RuntimeException("Refresh token expired");
        }

        long now = System.currentTimeMillis();
        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

        String newJwt = Jwts.builder()
                .setSubject(token.getUsername())
                .claim("roles", token.getRoles())
                .claim("emailAddress", userDetails.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + jwtExpirationInMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return Map.of(
                "message", "Access token refreshed",
                "token", newJwt,
                "expiresAt", Instant.ofEpochMilli(now + jwtExpirationInMs).toString()
        );
    }

    public void logout(String rawToken) {
        Optional<RefreshToken> stored = refreshTokenRepository.findByRawToken(rawToken);
        stored.ifPresent(token -> {
            // ❗️Prevent one user from logging out another user's session
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!token.getUsername().equals(currentUsername)) {
                throw new RuntimeException("Token does not belong to the authenticated user");
            }

            token.setExpired(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    public Optional<RefreshToken> findByRawToken(String rawToken) {
        return refreshTokenRepository.findByRawToken(rawToken);
    }

}

