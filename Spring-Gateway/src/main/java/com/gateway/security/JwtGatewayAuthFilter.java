package com.gateway.security;


import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;


/* JwtGatewayAuthFilter is a custom Global Filter in Spring Cloud Gateway (WebFlux)
that performs centralized JWT authentication and authorization for all incoming HTTP requests.

This filter ensures that:
Only requests with a valid Bearer token are allowed to access protected microservice routes.
Public endpoints like /login and /createCustomer remain accessible without a token.
*/

@Component
@Slf4j
public class JwtGatewayAuthFilter implements GlobalFilter {

    @Value("${jwt.secret}")
    private String secret;

    private Key key;

    // Define public endpoints here
    private final List<String> publicEndpoints = List.of(
            "/customers/createCustomer",
            "/login"
    );

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        log.info("🔎 Incoming request path: {}", path);

        // Allow public endpoints to pass without token
        if (publicEndpoints.stream().anyMatch(path::startsWith)) {
            log.info("🟢 Public endpoint '{}', skipping JWT filter", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("⛔ Missing or invalid Authorization header for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.info("✅ Token valid. Granting access to {}", path);
            return chain.filter(exchange);
        } catch (JwtException ex) {
            log.error("⛔ Invalid JWT: {}", ex.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}



