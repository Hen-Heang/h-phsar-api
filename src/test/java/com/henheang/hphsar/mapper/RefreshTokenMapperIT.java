package com.henheang.hphsar.mapper;

import com.henheang.hphsar.AbstractIntegrationTest;
import com.henheang.hphsar.model.jwt.RefreshToken;
import com.henheang.hphsar.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-Postgres coverage for RefreshTokenMapper.xml: insert/find round-trip
 * and the guarded revoke queries. Every email is tagged with a per-run unique
 * suffix — the container in AbstractIntegrationTest is a shared static
 * resource (no per-test rollback), so assertions must not assume they're
 * looking at the only rows in the table. Rotation/reuse-detection business
 * logic is covered separately in RefreshTokenServiceImplTest (mocked repository).
 */
class RefreshTokenMapperIT extends AbstractIntegrationTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private String uniqueEmail() {
        return "refresh-token-" + System.nanoTime() + "@example.test";
    }

    @Test
    void insertRefreshToken_returnsTheInsertedRowWithGeneratedId() {
        String email = uniqueEmail();
        Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(600));

        RefreshToken inserted = refreshTokenRepository.insertRefreshToken(email, 2, "hash-" + email, expiresAt);

        assertNotNull(inserted.getId());
        assertEquals(email, inserted.getEmail());
        assertEquals(2, inserted.getRoleId());
        assertEquals("hash-" + email, inserted.getTokenHash());
        assertNull(inserted.getRevokedAt());
    }

    @Test
    void findByTokenHash_unknownHash_returnsNull() {
        assertNull(refreshTokenRepository.findByTokenHash("no-such-hash-" + System.nanoTime()));
    }

    @Test
    void findByTokenHash_existingHash_returnsTheRow() {
        String email = uniqueEmail();
        String hash = "hash-" + email;
        refreshTokenRepository.insertRefreshToken(email, 1, hash, Timestamp.from(Instant.now().plusSeconds(600)));

        RefreshToken found = refreshTokenRepository.findByTokenHash(hash);

        assertNotNull(found);
        assertEquals(email, found.getEmail());
    }

    @Test
    void revokeByTokenHash_activeToken_updatesAndReturnsOneRowAffected() {
        String email = uniqueEmail();
        String hash = "hash-" + email;
        refreshTokenRepository.insertRefreshToken(email, 1, hash, Timestamp.from(Instant.now().plusSeconds(600)));

        Integer rowsAffected = refreshTokenRepository.revokeByTokenHash(hash);

        assertEquals(1, rowsAffected);
        assertNotNull(refreshTokenRepository.findByTokenHash(hash).getRevokedAt());
    }

    @Test
    void revokeByTokenHash_alreadyRevokedToken_isANoOp_returnsZeroRowsAffected() {
        String email = uniqueEmail();
        String hash = "hash-" + email;
        refreshTokenRepository.insertRefreshToken(email, 1, hash, Timestamp.from(Instant.now().plusSeconds(600)));
        refreshTokenRepository.revokeByTokenHash(hash);

        Integer secondAttempt = refreshTokenRepository.revokeByTokenHash(hash);

        assertEquals(0, secondAttempt,
                "revoking an already-revoked token must not report success a second time");
    }

    @Test
    void revokeByTokenHash_unknownHash_returnsZeroRowsAffected() {
        assertEquals(0, refreshTokenRepository.revokeByTokenHash("no-such-hash-" + System.nanoTime()));
    }

    @Test
    void revokeAllActiveByEmail_revokesEveryActiveTokenForThatEmailOnly() {
        String targetEmail = uniqueEmail();
        String otherEmail = uniqueEmail();
        String hash1 = "hash-a-" + targetEmail;
        String hash2 = "hash-b-" + targetEmail;
        String otherHash = "hash-" + otherEmail;

        refreshTokenRepository.insertRefreshToken(targetEmail, 1, hash1, Timestamp.from(Instant.now().plusSeconds(600)));
        refreshTokenRepository.insertRefreshToken(targetEmail, 1, hash2, Timestamp.from(Instant.now().plusSeconds(600)));
        refreshTokenRepository.insertRefreshToken(otherEmail, 1, otherHash, Timestamp.from(Instant.now().plusSeconds(600)));

        Integer rowsAffected = refreshTokenRepository.revokeAllActiveByEmail(targetEmail);

        assertEquals(2, rowsAffected);
        assertNotNull(refreshTokenRepository.findByTokenHash(hash1).getRevokedAt());
        assertNotNull(refreshTokenRepository.findByTokenHash(hash2).getRevokedAt());
        assertNull(refreshTokenRepository.findByTokenHash(otherHash).getRevokedAt(),
                "revoking by email must not touch a different account's tokens");
    }
}
