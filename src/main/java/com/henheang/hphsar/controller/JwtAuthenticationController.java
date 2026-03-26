package com.henheang.hphsar.controller;

import com.henheang.hphsar.config.JwtTokenUtil;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.model.appUser.AppUserDto;
import com.henheang.hphsar.model.appUser.AppUserRequest;
import com.henheang.hphsar.model.appUser.LoginResponse;
import com.henheang.hphsar.model.jwt.JwtChangePasswordRequest;
import com.henheang.hphsar.model.jwt.JwtRequest;
import com.henheang.hphsar.service.OtpService;
import com.henheang.hphsar.service.implement.JwtUserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * JwtAuthenticationController — Authentication Endpoints
 * <p>
 * All endpoints here are PUBLIC (no token required).
 * Configured in SecurityConfig → .requestMatchers("/authorization/**").permitAll()
 * <p>
 * Endpoints:
 * POST /authorization/register        → create a new account
 * POST /authorization/login           → login and receive JWT token
 * PUT  /authorization/change-password → change password (knows old password)
 * PUT  /authorization/forget          → reset password via OTP
 */
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/authorization")
@Tag(name = "API authorization")
@RequiredArgsConstructor
public class JwtAuthenticationController extends BaseController {

    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsServiceImpl jwtUserDetailsService;
    private final OtpService otpService;

    // FIX 7: Removed shared `Date` field — it was a class-level instance variable.
    // WHY: In a web app, multiple requests run in parallel (multithreaded).
    //      A shared `Date` field can be overwritten by another thread mid-request,
    //      causing wrong timestamps. Using `new Date()` locally inside each method is safe.

    // ─── Register ───────────────────────────────────────────────────────────────

    /**
     * PROCESS:
     * 1. Receive { email, password, roleId }
     * 2. Delegate to JwtUserDetailsServiceImpl.insertUser()
     * (validates, checks duplicate, Bcrypt hashes, inserts to DB)
     * 3. Return created user info (no password in response)
     * 4. @ReuqestBody :
     */
    @PostMapping("/register")
    public ResponseEntity<?> insertUser(@RequestBody AppUserRequest appUserRequest) {
        AppUserDto appUserDto = jwtUserDetailsService.insertUser(appUserRequest);
        return created("Successfully registered new user", appUserDto);
    }

    // ─── Login ───────────────────────────────────────────────────────────────────

    /**
     * PROCESS:
     * 1. Check if email is verified (is_verified = true in DB)
     * → If not: auto-send new OTP and return 409
     * 2. Authenticate email + password via Spring Security
     * → Internally: load user from DB + BCrypt.matches(raw, hash)
     * → If wrong password: throw 400
     * 3. Generate JWT token containing user's email
     * 4. Fetch roleId and userId to include in response
     * 5. Return { token, roleId, userId }
     */
    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        // Step 1: Block login if email not yet verified via OTP
        if (!verifyEmail(authenticationRequest.getEmail())) {
            otpService.generateOtp(authenticationRequest.getEmail());
            throw new ConflictException("Email is not verified. We just sent you a verification code.");
        }

        // Step 2: Verify email + password combination against DB
        authenticate(authenticationRequest.getEmail(), authenticationRequest.getPassword());

        // Step 3: Load user details and generate JWT token
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(authenticationRequest.getEmail());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Step 4: Get role and user ID to send back to the client
        Integer roleId = jwtUserDetailsService.getRoleIdByMail(authenticationRequest.getEmail());
        Integer userId = jwtUserDetailsService.getUserIdByMail(authenticationRequest.getEmail());
        return ok("Login Success", new LoginResponse(token, roleId, userId));
    }

    // ─── Change Password ─────────────────────────────────────────────────────────

    /**
     * PROCESS:
     * 1. Find user by email
     * 2. Verify old password matches stored Bcrypt hash
     * 3. Encode and save new password
     */
    @PutMapping(value = "/change-password")
    public ResponseEntity<?> changePassword(@RequestBody JwtChangePasswordRequest request) {
        return ok("Password changed successfully.", jwtUserDetailsService.changePassword(request));
    }

    // ─── Forget Password ──────────────────────────────────────────────────────────

    /**
     * PROCESS:
     * 1. Find user + their latest OTP from DB
     * 2. Validate OTP: email matches, code matches, not expired (< 3 min)
     * 3. Encode and save new password
     * 4. Delete OTP from DB (one-time use)
     * 5. Return success message only (FIX 1: no plain text password)
     */
    @PutMapping("/forget")
    public ResponseEntity<?> forgetPassword(@RequestParam Integer otp,
                                            @RequestParam String email,
                                            @RequestParam String newPassword) {
        return ok("Password reset successfully.", jwtUserDetailsService.forgetPassword(otp, email, newPassword));
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    /**
     * Delegates to Spring Security's AuthenticationManager to verify credentials.
     * Translates Spring Security exceptions into our custom exceptions.
     */
    private void authenticate(String email, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new BadRequestException("Invalid password. Please enter the correct password.");
        }
    }

    // FIX 6: Changed from public to private — this method is only used internally
    // WHY: Making it public would allow other classes to call it directly,
    //      which was never intended. Internal helper methods should be private.
    private boolean verifyEmail(String email) {
        return jwtUserDetailsService.getVerifyEmail(email);
    }
}