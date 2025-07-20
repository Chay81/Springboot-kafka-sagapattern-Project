package com.customer.util;

public class DataMaskingUtil {


    /**
     * Masks the email address by:
     * - Keeping first and last character of local part
     * - Masking domain part (except the TLD)
     * E.g. john.doe@example.com → j*******e@e******.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;

        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        // Mask local part
        String maskedLocal = localPart.length() <= 2
                ? localPart.charAt(0) + "*"
                : localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1);

        // Mask domain (except TLD)
        int lastDot = domainPart.lastIndexOf('.');
        if (lastDot == -1) return maskedLocal + "@" + "*".repeat(domainPart.length()); // fallback

        String domainName = domainPart.substring(0, lastDot);
        String tld = domainPart.substring(lastDot); // .com, .org, etc.

        String maskedDomain = domainName.charAt(0) + "*".repeat(domainName.length() - 1);

        return maskedLocal + "@" + maskedDomain + tld;
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return phone;
        return phone.substring(0, 2) + "******" + phone.substring(phone.length() - 2);
    }

    /**
     * Attempts to validate if a masked email corresponds to a known actual email.
     * This is **not a true unmasking** function.
     */
    public static String unmaskEmail(String maskedEmail, String actualEmail) {
        if (maskedEmail == null || actualEmail == null) return null;

        String recomputedMask = maskEmail(actualEmail);

        if (recomputedMask.equals(maskedEmail)) {
            return actualEmail;
        }

        // ⚠️ fallback: don't trust the masked one
        return maskedEmail;
    }
}

