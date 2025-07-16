package com.customer.service;

import com.customer.entity.RefreshToken;
import com.customer.loginmodels.AuthRequest;

import java.util.Map;
import java.util.Optional;

public interface AuthService {

    Map<String, String> login(AuthRequest request);

    Map<String, String> refreshToken(String rawToken, AuthRequest authRequest);

    void logout(String rawToken);

    Optional<RefreshToken> findByRawToken(String rawToken);

}
