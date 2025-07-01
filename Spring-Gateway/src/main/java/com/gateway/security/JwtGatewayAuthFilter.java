package com.gateway.security;


import com.gateway.constants.AppConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;


@Component
@Slf4j
public class JwtGatewayAuthFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secretKey;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // Define public endpoints here
    private final List<String> publicEndpoints = List.of(
            "/customers/createCustomer",
            "/login"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod httpMethod = request.getMethod();

        log.info("Incoming request path: {}", path);

        // Step 1: Bypass public endpoints
        if (publicEndpoints.stream().anyMatch(path::startsWith)) {
            log.info(" Public endpoint '{}', skipping JWT filter", path);
            return chain.filter(exchange);
        }

        //  Step 2: Extract and validate Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn(" Missing or invalid Authorization header for path: {}", path);
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, AppConstants.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        Claims claims;
        try {
            claims = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            log.info(" Token valid. Granting access to {}", path);

        } catch (ExpiredJwtException ex) {
            log.warn("❌ Token expired: {}", ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, AppConstants.EXPIRED_TOKEN);

        } catch (JwtException ex) {
            log.error("❌ Invalid JWT: {}", ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, AppConstants.INVALID_TOKEN);
        }


        // Step 3: Extract roles
        List<String> roles = claims.get("roles", List.class);
        log.info(" Role '{}' matched for path '{}'", roles, path);

        // Step 4: Perform role-based authorization
        // Allow only specific valid roles for /order-service
        if (path.startsWith("/orders") && !hasAllowedRole(roles,
                "ROLE_CUSTOMER", "ROLE_USER", "ROLE_ADMIN", "ROLE_MANAGER")) {

            log.warn("⛔ Access denied to /orders for roles: {}", roles);
            return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, AppConstants.ACCESS_DENIED_ORDER);
        }


        // ❌ Restrict /inventory-service to only ROLE_ADMIN
        if (path.startsWith("/inventory")) {

            // Allow GET requests for all roles
            if (HttpMethod.GET.equals(httpMethod)) {
                return chain.filter(exchange); // allow GET Method
            }

            // Restrict POST/PUT/DELETE to only ROLE_ADMIN
            if ((HttpMethod.POST.equals(httpMethod) || HttpMethod.PUT.equals(httpMethod) || HttpMethod.DELETE.equals(httpMethod))
                    && !hasAllowedRole(roles, "ROLE_ADMIN")) {

                log.warn("⛔ Access denied to modify inventory. Roles: {}", roles);
                return buildErrorResponse(exchange, HttpStatus.FORBIDDEN, AppConstants.ACCESS_DENIED_INVENTORY);
            }
        }


        // Step 5: Forward to next filter in chain
        return chain.filter(exchange);
    }

    private boolean hasAllowedRole(List<String> roles, String... allowedRoles) {
        return roles != null && roles.stream().anyMatch(allowed -> List.of(allowedRoles).contains(allowed));
    }

    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, HttpStatus status, String message) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json");

        String json = String.format("{\"error\": \"%s\"}", message);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(bytes);

        return exchange.getResponse().writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -1; // Run early in filter chain
    }
}



