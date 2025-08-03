package com.customer.service;

import com.customer.constants.AppConstants;
import com.customer.entity.Customer;
import com.customer.entity.RefreshToken;
import com.customer.loginmodels.AuthRequest;
import com.customer.loginmodels.AuthResponse;
import com.customer.repository.CustomerRepository;
import com.customer.repository.RefreshTokenRepository;
import com.customer.security.RSAEncryptor;
import com.customer.util.DataMaskingUtil;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
    private CustomerRepository customerRepository;

    @Autowired
    private RSAEncryptor rsaEncryptor;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpirationInMs;

    private static final long REFRESH_TOKEN_EXPIRY_MS = 30 * 60 * 1000;

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toCollection(HashSet::new));

        Customer customer = customerRepository.findByEmailAddress(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(AppConstants.CUSTOMER_NOT_FOUND));

        long now = System.currentTimeMillis();
        long expiryTime = now + jwtExpirationInMs;

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), AppConstants.HMACSHA256);

        String token = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .claim("emailAddress",rsaEncryptor.encrypt(userDetails.getUsername()))
                .claim("phoneNumber", rsaEncryptor.encrypt(customer.getPhoneNumber()))
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

        return AuthResponse.builder()
                .message("Login successful")
                .token(token)
                .expiresAt(formatToISTString(Instant.ofEpochMilli(expiryTime)))
                .refreshToken(rawRefreshToken)
//               Masking emailAddress and phoneNumber in Response
                .emailAddress(DataMaskingUtil.maskEmail(customer.getEmailAddress()))
                .phoneNumber(DataMaskingUtil.maskPhone(customer.getPhoneNumber()))
                .build();
    }

    public AuthResponse refreshToken(
            String rawToken, AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Customer customer = customerRepository.findByEmailAddress(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException(AppConstants.CUSTOMER_NOT_FOUND));

        Optional<RefreshToken> optionalToken = refreshTokenRepository.findByRawToken(rawToken);

        if (optionalToken.isEmpty()) {
            throw new RuntimeException(AppConstants.INVALID_REFRESHTOKEN);
        }

        RefreshToken token = optionalToken.get();

        // 🔐 Ownership validation: ensure token belongs to the authenticated user
        if (!token.getUsername().equals(userDetails.getUsername())) {
            throw new RuntimeException(AppConstants.REFRESH_TOKEN_USER);
        }

        if (token.isExpired() || token.getExpiresAt().isBefore(Instant.now())) {
            token.setExpired(true);
            refreshTokenRepository.save(token);
            throw new RuntimeException(AppConstants.REFRESH_TOKEN_EXPIRY);
        }

        long now = System.currentTimeMillis();
        long expiryTime = now + jwtExpirationInMs;

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), AppConstants.HMACSHA256);

        String newJwt = Jwts.builder()
                .setSubject(token.getUsername())
                .claim("roles", token.getRoles())
                .claim("emailAddress",rsaEncryptor.encrypt(userDetails.getUsername()))
                .claim("phoneNumber", rsaEncryptor.encrypt(customer.getPhoneNumber()))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(expiryTime))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        return AuthResponse.builder()
                .message("Access token refreshed")
                .token(newJwt)
                .expiresAt(formatToISTString(Instant.ofEpochMilli(expiryTime)))
//               Masking emailAddress and phoneNumber
                .emailAddress(DataMaskingUtil.maskEmail(customer.getEmailAddress()))
                .phoneNumber(DataMaskingUtil.maskPhone(customer.getPhoneNumber()))
                .build();
    }

    public void logout(String rawToken) {
        Optional<RefreshToken> stored = refreshTokenRepository.findByRawToken(rawToken);
        stored.ifPresent(token -> {
            // ❗️Prevent one user from logging out another user's session
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!token.getUsername().equals(currentUsername)) {
                throw new RuntimeException(AppConstants.TOKEN_NOT_BELONG_USER);
            }

            token.setExpired(true);
            refreshTokenRepository.save(token);
        });
    }

    @Override
    public Optional<RefreshToken> findByRawToken(String rawToken) {
        return refreshTokenRepository.findByRawToken(rawToken);
    }

    // 🔁 IST Time Formatter
    private String formatToISTString(Instant instant) {
        ZonedDateTime istTime = instant.atZone(ZoneId.of("Asia/Kolkata"));
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(istTime);
    }
}

