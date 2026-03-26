package com.henheang.hphsar.controller.otp;

import com.henheang.hphsar.controller.BaseController;
import com.henheang.hphsar.exception.BadRequestException;
//import com.henheang.hphsar.model.ApiResponse;
import com.henheang.hphsar.service.OtpService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.henheang.hphsar.common.api.ApiResponse;
/**
 * OTPController — OTP Endpoints
 * <p>
 * All endpoints here are PUBLIC (no token required).
 * Configured in SecurityConfig → .requestMatchers("/authorization/**").permitAll()
 * <p>
 * Endpoints:
 *   POST /authorization/api/v1/otp/generate → send OTP code to email
 *   POST /authorization/api/v1/otp/verify   → verify OTP code and activate account
 */
@RestController
@CrossOrigin(origins = "*")
@Tag(name = "Generate OTP")
@RequestMapping("/authorization/api/v1/otp")
@RequiredArgsConstructor
public class OTPController extends BaseController {

    private final OtpService otpService;

    // FIX 7: Removed shared `Date` field — replaced with local new Date() per method
    // WHY: Shared mutable state across concurrent requests can cause wrong timestamps.

    // ─── Generate OTP ───────────────────────────────────────────────────────────

    /**
     * PROCESS:
     *   1. Validate user exists by email
     *   2. FIX 8: Check if valid OTP already sent recently (rate limiting)
     *   3. Generate random 4-digit OTP
     *   4. Save to DB with timestamp
     *   5. Send OTP to email
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateOtp(@RequestParam String email) {
        String otpResponse = otpService.generateOtp(email);
        return ok("New OTP generated.", otpResponse);
    }

    // ─── Verify OTP ─────────────────────────────────────────────────────────────

    /**
     * PROCESS:
     *   1. Validate OTP is within Integer range
     *   2. Load user + latest OTP from DB
     *   3. Validate: email matches, code matches, not expired (< 3 min)
     *   4. Mark account as verified in DB (is_verified = true)
     *   5. FIX 3: Delete OTP from DB after successful use
     */
    @PostMapping("/verify")
    public ResponseEntity<?> activateAccount(@RequestParam Integer otp, @RequestParam String email) {
        // Guard: Java Integer max is 2147483647 — prevent overflow issues
        if (otp > 2147483646) {
            throw new BadRequestException("Integer value cannot exceed 2147483646.");
        }
        return ok("Account activated successfully.", otpService.verifyOtp(otp, email));
    }
}