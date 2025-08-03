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

    public static final String ALPHANUMERIC_CHARACTERS_STRONG_PASSWORD =  "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$";
//    Explanation:
//            (?=.*[a-z]) → At least one lowercase letter

//            (?=.*[A-Z]) → At least one uppercase letter

//            (?=.*\\d) → At least one digit

//            (?=.*[@$!%*?&]) → At least one special character

//            [A-Za-z\\d@$!%*?&]{12,} → Allows only these characters, total length ≥ 12

    public static final String PASSWORD_FAIL_8_CHARACTERS = "Password must contain one Uppercase letter and minimum one Lowercase letter" +
            " and at least 12 characters long and should have one special character.";

    public static final String PASSWORD_REQUIRED = "Password and retype password are required.";

    public static final String PASSWORD_OLD_MATCH = "New password must be different from the old password.";

    public static final String PASSWORD_MISMATCH = "New password and retype password do not match.";

    public static final String CUSTOMER_NOT_FOUND = "Customer not found with provided email or phone number.";

    public static final String CUSTOMER_NOT_AVAILABLE = "Customer not found with ID: ";

    public static final String PHONE_NOT_FOUND = "Phone number does not match the provided email address.";

    public static final String PROVIDE_EMAIL_OR_PHONE = "Either email or phone number must be provided.";

    public static final String PASSWORD_SUCCESS = "Password updated successfully.";

    public static final String HMACSHA256 = "HmacSHA256";

    public static final String INVALID_REFRESHTOKEN= "Invalid refresh token";

    public static final String REFRESH_TOKEN_USER = "Refresh token does not belong to this user";

    public static final String REFRESH_TOKEN_EXPIRY = "Refresh token expired";

    public static final String TOKEN_NOT_BELONG_USER= "Token does not belong to the authenticated user";

    public static final String DELETE_CUSTOMER = "You are not authorized to delete another customer's account.";

    public static final String VIEW_CUSTOMER = "You are not authorized to view this customer's data.";

    public static final String MODIFY_CUSTOMER = "You are not authorized to modify another customer's data.";
}
