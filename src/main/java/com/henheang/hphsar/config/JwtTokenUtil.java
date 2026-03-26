package com.henheang.hphsar.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serial;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * JwtTokenUtil — JWT Token Helper
 * <p>
 * Handles all JWT operations:
 *   1. generateToken()  → create a signed token after login
 *   2. validateToken()  → verify token is valid and not expired
 *   3. getUsernameFromToken() → extract user's email/username from token
 * <p>
 * A JWT token looks like:  xxxxx.yyyyy.zzzzz
 *   - xxxxx = Header  (algorithm used)
 *   - yyyyy = Payload (claims: email, issued time, expiry)
 *   - zzzzz = Signature (proves the token was not tampered with)
 * <p>
 * The secret key (from application.properties: jwt. Secret) is used
 * to sign tokens when generating, and to verify them when validating.
 * <p>
 * Token lifetime: 24 hours (JWT_TOKEN_VALIDITY)
 * <p>
 * Used by:
 *   - JwtAuthenticationController → generateToken() after successful login
 *   - JwtRequestFilter            → getUsernameFromToken() + validateToken() on every request
 */
@Component
public class JwtTokenUtil implements Serializable {

    @Serial
    private static final long serialVersionUID = -2550188375426007488L;

    // Token is valid for 24 hours (in seconds)
    public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60;

    // Secret key loaded from application.properties: jwt.secret=your-secret-key
    @Value("${jwt.secret}")
    private String secret;

    // ─── Public API ────────────────────────────────────────────────────────────

    /** Extract the user's email (subject) from the token */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /** Extract the expiration date from the token */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Generate a new JWT token for the given user.
     * The token's subject is set to the user's email/username.
     * Called once after a successful login.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * Validate that:
     *   1. The token belongs to the given user (username matches)
     *   2. The token has not expired
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ─── Internal Helpers ──────────────────────────────────────────────────────

    /** Extract any specific claim from the token using a resolver function */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /** Decode and return all claims from the token (requires the secret key to verify signature) */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Returns true if the token's expiration date is in the past */
    private Boolean isTokenExpired(String token) {
        return getExpirationDateFromToken(token).before(new Date());
    }

    /**
     * Builds and signs the JWT token:
     *   - Subject   : user's email
     *   - IssuedAt  : now
     *   - Expiration: now + 24 hours
     *   - Signature : HS512 algorithm with the secret key
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    /** Builds the HMAC-SHA512 signing key from the configured secret string */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA512");
    }
}