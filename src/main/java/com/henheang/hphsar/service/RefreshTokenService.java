package com.henheang.hphsar.service;

import com.henheang.hphsar.model.jwt.RefreshedSession;

public interface RefreshTokenService {

    // 14 days — long-lived compared to the 15-minute access token
    // (JwtTokenUtil.JWT_TOKEN_VALIDITY). Shared by the controller so the
    // Set-Cookie Max-Age always matches what was actually persisted.
    long REFRESH_TOKEN_VALIDITY_SECONDS = 14L * 24 * 60 * 60;

    /** Generates, persists, and returns a new raw refresh token for the given account. */
    String issue(String email, Integer roleId);

    /**
     * Validates a raw refresh token (from the cookie), rotates it (revokes the
     * old one, issues a new one), and returns the account + new raw token.
     * Throws UnauthorizedException if missing/expired/revoked/unknown.
     */
    RefreshedSession validateAndRotate(String rawToken);

    /** Revokes a raw refresh token (logout). Safe to call with an already-revoked or unknown token. */
    void revoke(String rawToken);
}
