package com.henheang.hphsar.controller;

import com.henheang.hphsar.config.JwtTokenUtil;
import com.henheang.hphsar.exception.BadRequestException;
import com.henheang.hphsar.exception.ConflictException;
import com.henheang.hphsar.exception.UnauthorizedException;
import com.henheang.hphsar.model.appUser.AppUserDto;
import com.henheang.hphsar.model.appUser.AppUserRequest;
import com.henheang.hphsar.model.appUser.LoginResponse;
import com.henheang.hphsar.model.jwt.AccessTokenResponse;
import com.henheang.hphsar.model.jwt.JwtChangePasswordRequest;
import com.henheang.hphsar.model.jwt.JwtRequest;
import com.henheang.hphsar.model.jwt.RefreshedSession;
import com.henheang.hphsar.service.OtpService;
import com.henheang.hphsar.service.RefreshTokenService;
import com.henheang.hphsar.service.implement.JwtUserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
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
 * POST /authorization/login           → login, receive a short-lived JWT + a refresh-token cookie
 * POST /authorization/refresh         → exchange the refresh-token cookie for a new JWT (rotates the cookie)
 * POST /authorization/logout          → revoke the refresh token and clear its cookie
 * PUT  /authorization/change-password → change password (knows old password)
 * PUT  /authorization/forget          → reset password via OTP
 * <p>
 * CORS for these endpoints (including the credentialed cookie requests above)
 * is handled globally by CorsFilterConfiguration — no per-controller
 * @CrossOrigin here, since "origins = *" combined with a credentialed cookie
 * would defeat the point of scoping the refresh cookie to explicit origins.
 */
@RestController
@RequestMapping("/authorization")
@Tag(name = "API authorization")
@RequiredArgsConstructor
public class JwtAuthenticationController extends BaseController {

    // Path scoped to the whole /authorization/** prefix (not just /refresh) so
    // that /authorization/login (sets it), /authorization/refresh (reads +
    // rotates it), and /authorization/logout (reads + clears it) can all see
    // the cookie — browsers only attach a cookie whose Path is a prefix of the
    // request path, so scoping it to /authorization/refresh alone would make
    // /logout unable to read it.
    private static final String REFRESH_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_COOKIE_PATH = "/authorization";

    private final JwtTokenUtil jwtTokenUtil;
    private final AuthenticationManager authenticationManager;
    private final JwtUserDetailsServiceImpl jwtUserDetailsService;
    private final OtpService otpService;
    private final RefreshTokenService refreshTokenService;

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
     * 3. Generate a short-lived JWT access token containing user's email
     * 4. Fetch roleId and userId to include in response
     * 5. Issue a long-lived refresh token and set it as an HttpOnly cookie
     * 6. Return { token, roleId, userId } — response body shape unchanged from before
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

        // Step 5: Issue a refresh token for this session and set it as a cookie
        String rawRefreshToken = refreshTokenService.issue(authenticationRequest.getEmail(), roleId);

        return ok("Login Success", new LoginResponse(token, roleId, userId), refreshCookie(rawRefreshToken));
    }

    // ─── Refresh ─────────────────────────────────────────────────────────────────

    /**
     * PROCESS:
     * 1. Read the refreshToken cookie (no Authorization header expected/required here)
     * 2. Validate it's not missing/expired/revoked/unknown; rotate it (old one
     *    revoked, new one issued) — see RefreshTokenServiceImpl for the
     *    stolen-token reuse-detection behavior on step 2
     * 3. Generate a new short-lived access token for the same account
     * 4. Return { token } and set the rotated refresh-token cookie
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie) {

        if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
            throw new UnauthorizedException("Missing refresh token.");
        }

        RefreshedSession session = refreshTokenService.validateAndRotate(refreshTokenCookie);

        UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(session.getEmail());
        String newAccessToken = jwtTokenUtil.generateToken(userDetails);

        return ok("Token refreshed successfully.",
                new AccessTokenResponse(newAccessToken),
                refreshCookie(session.getNewRawToken()));
    }

    // ─── Logout ──────────────────────────────────────────────────────────────────

    /**
     * PROCESS:
     * 1. Read the refreshToken cookie, if present
     * 2. Revoke it server-side (idempotent — safe if already revoked/missing)
     * 3. Clear the cookie (Max-Age=0)
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(value = REFRESH_COOKIE_NAME, required = false) String refreshTokenCookie) {

        if (refreshTokenCookie != null && !refreshTokenCookie.isBlank()) {
            refreshTokenService.revoke(refreshTokenCookie);
        }

        return ok("Logged out successfully.", "You have been logged out.", expiredRefreshCookie());
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

    /**
     * Builds the Set-Cookie header for a (re)issued refresh token.
     * sameSite("None"): the frontend and this API are deployed as separate
     * Render services (different subdomains/domains), which is cross-site for
     * cookie purposes — SameSite=Lax would silently drop this cookie on the
     * cross-site POSTs to /refresh and /logout. None requires Secure, which
     * is already set. secure(true): Chrome/Firefox/Safari all treat
     * http://localhost as a secure context exception, so this still works in
     * local dev without HTTPS as long as the frontend origin is also
     * localhost — but a non-localhost plain-HTTP dev setup (e.g. a LAN IP)
     * would need HTTPS too.
     */
    private ResponseCookie refreshCookie(String rawRefreshToken) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, rawRefreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(RefreshTokenService.REFRESH_TOKEN_VALIDITY_SECONDS)
                .build();
    }

    /** Same attributes as refreshCookie() (Path/SameSite/etc. must match to actually clear it), Max-Age=0. */
    private ResponseCookie expiredRefreshCookie() {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path(REFRESH_COOKIE_PATH)
                .maxAge(0)
                .build();
    }
}