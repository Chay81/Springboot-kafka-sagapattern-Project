package com.gateway.constants;

import java.util.List;

public class AppConstants {

    public static final String UNAUTHORIZED = "Unauthorized: Missing or invalid token";

    public static final String EXPIRED_TOKEN = "❌ Session has expired";

    public static final String INVALID_TOKEN = "❌ Invalid JWT";

    public static final String ACCESS_DENIED_ORDER = "Access denied: You are not allowed to place orders";

    public static final String ACCESS_DENIED_INVENTORY = "Only admins can modify inventory";

    public static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/customers/createCustomer",
            "/login",
            "/customer/forgotPassword"
    );

}
