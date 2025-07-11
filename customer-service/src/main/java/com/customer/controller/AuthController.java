package com.customer.controller;

import com.customer.DTO.RefreshTokenRequestDTO;
import com.customer.entity.RefreshToken;
import com.customer.loginmodels.AuthRequest;
import com.customer.repository.RefreshTokenRepository;
import com.customer.util.RSAEncryptor;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RSAEncryptor rsaEncryptor;

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // set to 1800000 in application.yml or .properties
    private long jwtExpirationInMs;

    // Refresh token with 30 minutes
    private static final long REFRESH_TOKEN_EXPIRY_MS = 30 * 60 * 1000;

    /**
     * LOGIN endpoint → generates JWT + refresh token.
     */

    @PostMapping("/login")
    @Transactional
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // 1. Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // 2. Load user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            Set<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toCollection(HashSet::new));


            //           Below Method is not needed as we are getting user details from userDetails shown above.
            //            Customer customer = customerRepository.findByEmailAddress(request.getEmail())
            //                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // expiryTime declaration
            long now = System.currentTimeMillis();
            long expiryTime = now + jwtExpirationInMs;
            // 3. Prepare signing key
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            // 4. Create JWT token with roles
            String token = Jwts.builder()
                    .setSubject(userDetails.getUsername())
                    .claim("roles", roles)
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(expiryTime)) // 30 minutes
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            log.info("🔐 JWT token generated for user: {}", userDetails.getUsername());

            // Generate Refresh Token (random UUID, RSA-encrypted)
            String rawRefreshToken = UUID.randomUUID().toString();
            String encryptedToken = rsaEncryptor.encrypt(rawRefreshToken);

            log.info("🔁 Refresh token generated (unencrypted): {}", rawRefreshToken);

            // Save to DB
            refreshTokenRepository.deleteByUsername(userDetails.getUsername()); // remove old

            RefreshToken refreshTokenEntity = RefreshToken.builder()
                    .token(encryptedToken)
                    .rawToken(rawRefreshToken)
                    .username(userDetails.getUsername())
                    .roles(roles)
                    .issuedAt(Instant.ofEpochMilli(now))
                    .expiresAt(Instant.ofEpochMilli(now + REFRESH_TOKEN_EXPIRY_MS))
                    .expired(false)
                    .build();

            log.info("🧪 IssuedAt: {}, ExpiresAt: {}", refreshTokenEntity.getIssuedAt(), refreshTokenEntity.getExpiresAt());
            refreshTokenRepository.save(refreshTokenEntity);

            // 5. Return token
            Map<String, String> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("token", token);

            // build token expiry response
            response.put("expiresAt", Instant.ofEpochMilli(expiryTime).toString());
            // refresh token
            response.put("refreshToken", rawRefreshToken);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of
                    ("error", "Unauthorized", "message", "Invalid credentials or customer not found"));
        }
    }

    /**
     * REFRESH TOKEN endpoint → validates stored token and issues new JWT.
     */

    @PostMapping("/refreshToken")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequestDTO request) {

        try {
            String rawToken = request.getRefreshToken();
            Optional<RefreshToken> optionalToken = refreshTokenRepository.findByRawToken(rawToken);

            if (optionalToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Unauthorized", "message", "Invalid refresh token"));
            }

            RefreshToken token = optionalToken.get();
            if (token.isExpired() || token.getExpiresAt().isBefore(Instant.now())) {
                token.setExpired(true);
                refreshTokenRepository.save(token);

                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                        "error", "Unauthorized",
                        "message", "Refresh token expired"));
            }

            // ✅ Generate new JWT
            long now = System.currentTimeMillis();
            SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

            String newJwt = Jwts.builder()
                    .setSubject(token.getUsername())
                    .claim("roles", token.getRoles())
                    .setIssuedAt(new Date(now))
                    .setExpiration(new Date(now + jwtExpirationInMs))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            return ResponseEntity.ok(Map.of(
                    "message", "Access token refreshed",
                    "token", newJwt,
                    "expiresAt", Instant.ofEpochMilli(now + jwtExpirationInMs).toString()));

        } catch (Exception e) {
            log.error("❌ Error refreshing token: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Unauthorized", "message", "Invalid refresh token"));
        }
    }


    @PostMapping("/customer/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> request) {
        String rawToken = request.get("refreshToken");

        log.info("🔒 Logout endpoint was triggered");
        log.info("📥 Received refresh token: {}", rawToken);

        if (rawToken == null || rawToken.isEmpty()) {
            return ResponseEntity.badRequest().body("Refresh token is required for logout.");
        }

        Optional<RefreshToken> stored = refreshTokenRepository.findByRawToken(rawToken);

        if (stored.isPresent()) {
            RefreshToken token = stored.get();
            token.setExpired(true);
            refreshTokenRepository.save(token);

            log.info("✅ Logged out user: {}", token.getUsername());
            return ResponseEntity.ok("Logout successful");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Refresh token not found");
        }
    }
}
