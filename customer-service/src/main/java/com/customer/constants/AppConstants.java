package com.customer.constants;

import java.util.Set;

public class AppConstants {

    public static final Set<String> VALID_ROLES = Set.of("ROLE_CUSTOMER", "ROLE_ADMIN");

    public static final String[] PUBLIC_PATHS = {
            "/customers/createCustomer",
            "/login",
            "/refreshToken",
            "/customer/forgotPassword"
    };

    public static final String ALPHANUMERIC_CHARACTERS = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    public static final String PASSWORD_FAIL_8_CHARACTERS = "Password must be at least 8 alphanumeric characters.";

    public static final String PASSWORD_OLD_MATCH = "New password must be different from the old password.";

    public static final String PASSWORD_MISMATCH = "New password and retype password do not match.";

    public static final String CUSTOMER_NOT_FOUND = "Customer not found with provided email or phone number.";

    public static final String PASSWORD_SUCCESS = "Password updated successfully.";

}
