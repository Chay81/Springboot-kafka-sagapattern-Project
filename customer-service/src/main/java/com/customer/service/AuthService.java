package com.customer.service;

import com.customer.entity.RefreshToken;
import com.customer.loginmodels.AuthRequest;
import com.customer.loginmodels.AuthResponse;

import java.util.Map;
import java.util.Optional;

public interface AuthService {

    AuthResponse login(AuthRequest request);

    AuthResponse refreshToken(String rawToken, AuthRequest authRequest);

    void logout(String rawToken);

    Optional<RefreshToken> findByRawToken(String rawToken);

}
