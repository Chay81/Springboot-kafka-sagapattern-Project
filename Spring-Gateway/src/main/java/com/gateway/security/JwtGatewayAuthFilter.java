package com.gateway.security;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.core.io.buffer.DataBufferUtils;
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
import java.util.Map;


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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        HttpMethod httpMethod = request.getMethod();

        log.info("Incoming request path: {}", path);

        // Step 1: Bypass public endpoints
        if (AppConstants.PUBLIC_ENDPOINTS.stream().anyMatch(path::startsWith)) {
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
            String emailAddress = claims.get("emailAddress", String.class);
            List<String> roles = claims.get("roles", List.class);

            // Inject email into request header for downstream services
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Authenticated-Email", emailAddress)
                    .header("X-Authenticated-Roles", String.join(",", roles))
                    .build();

            // Use the mutated request in the exchange
            exchange = exchange.mutate().request(mutatedRequest).build();


        } catch (ExpiredJwtException ex) {
            log.warn("❌ Token expired: {}", ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, AppConstants.EXPIRED_TOKEN);

        } catch (JwtException ex) {
            log.error("❌ Invalid JWT: {}", ex.getMessage());
            return buildErrorResponse(exchange, HttpStatus.UNAUTHORIZED, AppConstants.INVALID_TOKEN);
        }

        // for forgot password
        if (request.getURI().getPath().contains("/customers/forgotPassword")) {
            return chain.filter(exchange); // 🔓 Skip auth for forgot password
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

            if (path.startsWith("/admin") && !hasAllowedRole(roles, "ROLE_ADMIN")) {
                log.warn("Access denied: Only ROLE_ADMIN can access admin endpoints");
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

        }

        // Validate that the customer can only update their own data by matching emailAddress with their token in JWT vs request body
        ObjectMapper objectMapper = new ObjectMapper();

        if (path.startsWith("/customers") &&
                (HttpMethod.PUT.equals(httpMethod) || HttpMethod.PATCH.equals(httpMethod) || HttpMethod.DELETE.equals(httpMethod))) {

            ServerWebExchange finalExchange = exchange;
            return DataBufferUtils.join(exchange.getRequest().getBody())
                    .flatMap(dataBuffer -> {
                        byte[] bodyBytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bodyBytes);
                        DataBufferUtils.release(dataBuffer);

                        try {
                            String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
                            Map<String, Object> requestBodyMap = objectMapper.readValue(bodyString, Map.class);

                            String emailFromBody = (String) requestBodyMap.get("emailAddress");
                            String emailFromToken = finalExchange.getRequest().getHeaders().getFirst("X-Authenticated-Email");

                            if (emailFromToken != null && emailFromBody != null &&
                                    !emailFromToken.equalsIgnoreCase(emailFromBody)) {

                                log.warn("⛔ Email mismatch! Token={} vs Body={}", emailFromToken, emailFromBody);
                                return buildErrorResponse(finalExchange, HttpStatus.FORBIDDEN, "Unauthorized to modify another customer's data.");
                            }

                            // Rebuild request with original body
                            ServerHttpRequest mutatedRequest = finalExchange.getRequest().mutate()
                                    .header("X-Authenticated-Email", emailFromToken)
                                    .build();

                            DataBuffer newBody = finalExchange.getResponse().bufferFactory().wrap(bodyBytes);
                            ServerWebExchange mutatedExchange = finalExchange.mutate().request(mutatedRequest).build();

                            return chain.filter(mutatedExchange);
                        } catch (Exception e) {
                            log.error("❌ Failed to parse request body for ownership check", e);
                            return buildErrorResponse(finalExchange, HttpStatus.BAD_REQUEST, "Invalid request body.");
                        }
                    });
        }

// Default for all other routes
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



