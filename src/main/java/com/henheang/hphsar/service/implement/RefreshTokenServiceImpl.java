package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.UnauthorizedException;
import com.henheang.hphsar.model.jwt.RefreshToken;
import com.henheang.hphsar.model.jwt.RefreshedSession;
import com.henheang.hphsar.repository.RefreshTokenRepository;
import com.henheang.hphsar.service.RefreshTokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;

/**
 * RefreshTokenServiceImpl — refresh-token issue/validate/rotate/revoke.
 * <p>
 * The raw token is a 256-bit random value (SecureRandom), Base64URL-encoded
 * for use as a cookie value. Only its SHA-256 hash is ever persisted or
 * looked up — matching-by-hash means a database read alone can never reveal
 * a usable token, the same reason passwords are hashed rather than stored.
 */
@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenServiceImpl(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    @Transactional
    public String issue(String email, Integer roleId) {
        String rawToken = generateRawToken();
        Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(REFRESH_TOKEN_VALIDITY_SECONDS));
        refreshTokenRepository.insertRefreshToken(email, roleId, hash(rawToken), expiresAt);
        return rawToken;
    }

    @Override
    // noRollbackFor: the reuse-detection branch below deliberately revokes every
    // active token for the account and THEN throws — the revocation must commit
    // even though the method ends in an exception, otherwise the breach response
    // itself would be rolled back and never take effect.
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public RefreshedSession validateAndRotate(String rawToken) {
        String tokenHash = hash(rawToken);
        RefreshToken found = refreshTokenRepository.findByTokenHash(tokenHash);

        if (found == null) {
            throw new UnauthorizedException("Invalid refresh token.");
        }
        if (found.getRevokedAt() != null) {
            // A rotated-away (or already logged-out) token being presented again
            // is the classic refresh-token-theft signal: kill every other active
            // session for this account rather than just failing this one request.
            refreshTokenRepository.revokeAllActiveByEmail(found.getEmail());
            throw new UnauthorizedException("Refresh token has been revoked.");
        }
        if (found.getExpiresAt().before(new Date())) {
            throw new UnauthorizedException("Refresh token has expired.");
        }

        // Guarded UPDATE (WHERE ... revoked_at IS NULL): the earlier findByTokenHash
        // read isn't a lock, so two concurrent requests presenting the same
        // still-valid token can both reach this point having both seen
        // revokedAt == null. Only one revoke can actually flip the row (the
        // affected-row count tells them apart) — the loser must fail here
        // instead of also minting a new token, or the same token would be
        // able to spawn two live sibling sessions.
        Integer rowsRevoked = refreshTokenRepository.revokeByTokenHash(tokenHash);
        if (rowsRevoked == null || rowsRevoked == 0) {
            throw new UnauthorizedException("Refresh token has already been used.");
        }

        String newRawToken = issue(found.getEmail(), found.getRoleId());
        return new RefreshedSession(found.getEmail(), newRawToken);
    }

    @Override
    public void revoke(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }
        refreshTokenRepository.revokeByTokenHash(hash(rawToken));
    }

    private String generateRawToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a mandatory JDK algorithm — this can only mean a broken JVM.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
