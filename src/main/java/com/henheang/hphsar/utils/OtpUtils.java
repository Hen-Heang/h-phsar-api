package com.henheang.hphsar.utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * OtpUtils — Shared OTP Helper
 * <p>
 * WHY THIS CLASS EXISTS:
 *   The same "check if OTP is expired" logic was copied in two places:
 *     - OtpServiceImplV1.java
 *     - JwtUserDetailsServiceImpl.java
 *   If we need to change 3 minutes to 5 minutes, we would have to update
 *   both files and risk forgetting one. This utility centralizes it in one place.
 * <p>
 * HOW IT WORKS:
 *   - OTP_EXPIRY_MINUTES = 3 → OTP is only valid for 3 minutes after creation
 *   - isNotExpired() → returns true if OTP is still within the 3-minute window
 * <p>
 * USED BY:
 *   - OtpServiceImplV1   → verifyOtp() and generateOtp()
 *   - JwtUserDetailsServiceImpl → forgetPassword()
 */
public class OtpUtils {

    // How long (in minutes) an OTP code is valid after it was generated
    // Change this one value to update the expiry everywhere in the app
    public static final long OTP_EXPIRY_MINUTES = 3;

    /**
     * Checks whether an OTP is still valid (not expired).
     * <p>
     * HOW IT WORKS:
     *   1. Get current time
     *   2. Subtract OTP created time
     *   3. Convert difference to minutes
     *   4. If difference < 3 minutes → still valid → return true
     *      If difference >= 3 minutes → expired → return false
     *
     * @param createdDate the timestamp when the OTP was generated
     * @return true if OTP is still valid, false if expired
     */
    public static boolean isNotExpired(Date createdDate) {
        long diffInMillis = Math.abs(new Date().getTime() - createdDate.getTime());
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis);
        return diffInMinutes < OTP_EXPIRY_MINUTES;
    }
}