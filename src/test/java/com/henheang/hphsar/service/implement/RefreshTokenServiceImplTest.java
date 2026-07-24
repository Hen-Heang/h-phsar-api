package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.exception.UnauthorizedException;
import com.henheang.hphsar.model.jwt.RefreshToken;
import com.henheang.hphsar.model.jwt.RefreshedSession;
import com.henheang.hphsar.repository.RefreshTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pure unit coverage of RefreshTokenServiceImpl with a mocked repository —
 * no Spring context, no database. Mapper SQL correctness (insert/find/revoke
 * queries) belongs in a Testcontainers IT instead (see RefreshTokenMapperIT).
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @Test
    void issue_persistsAHashNotTheRawToken_andReturnsTheRawToken() {
        String rawToken = refreshTokenService.issue("buyer@example.test", 2);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).insertRefreshToken(
                eq("buyer@example.test"), eq(2), hashCaptor.capture(), any(Timestamp.class));

        assertNotNull(rawToken);
        assertFalse(rawToken.isBlank());
        assertNotEquals(rawToken, hashCaptor.getValue(), "the persisted value must be a hash, never the raw token");
        assertEquals(64, hashCaptor.getValue().length(), "SHA-256 hex digest is always 64 chars");
    }

    @Test
    void validateAndRotate_unknownToken_throwsUnauthorized() {
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(null);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.validateAndRotate("some-raw-token"));
        verify(refreshTokenRepository, never()).revokeByTokenHash(anyString());
    }

    @Test
    void validateAndRotate_expiredToken_throwsUnauthorized() {
        RefreshToken expired = new RefreshToken(1, "s@example.test", 1, "hash",
                Timestamp.from(Instant.now().minusSeconds(60)), null, Timestamp.from(Instant.now()));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(expired);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.validateAndRotate("raw"));
        verify(refreshTokenRepository, never()).revokeByTokenHash(anyString());
    }

    @Test
    void validateAndRotate_alreadyRevokedToken_revokesEveryActiveTokenForTheAccount_thenThrows() {
        RefreshToken revoked = new RefreshToken(1, "victim@example.test", 1, "hash",
                Timestamp.from(Instant.now().plusSeconds(600)),
                Timestamp.from(Instant.now().minusSeconds(1)),
                Timestamp.from(Instant.now()));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(revoked);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.validateAndRotate("stolen-raw-token"));

        verify(refreshTokenRepository).revokeAllActiveByEmail("victim@example.test");
        verify(refreshTokenRepository, never()).insertRefreshToken(anyString(), any(), anyString(), any());
    }

    @Test
    void validateAndRotate_validToken_revokesOldAndIssuesNew() {
        // The service re-hashes the presented raw token to look it up and to
        // revoke it — it never trusts a stored tokenHash field for the revoke
        // call, so the expected hash here must be derived from "raw-token"
        // itself, not an arbitrary placeholder.
        RefreshToken active = new RefreshToken(1, "s@example.test", 1, "irrelevant-stored-hash-value",
                Timestamp.from(Instant.now().plusSeconds(600)), null, Timestamp.from(Instant.now()));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(active);
        when(refreshTokenRepository.revokeByTokenHash(anyString())).thenReturn(1);

        RefreshedSession result = refreshTokenService.validateAndRotate("raw-token");

        ArgumentCaptor<String> lookedUpHash = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> revokedHash = ArgumentCaptor.forClass(String.class);
        verify(refreshTokenRepository).findByTokenHash(lookedUpHash.capture());
        verify(refreshTokenRepository).revokeByTokenHash(revokedHash.capture());
        assertEquals(lookedUpHash.getValue(), revokedHash.getValue(),
                "must revoke the exact token that was looked up, not some other value");

        verify(refreshTokenRepository).insertRefreshToken(eq("s@example.test"), eq(1), anyString(), any(Timestamp.class));
        assertEquals("s@example.test", result.getEmail());
        assertNotNull(result.getNewRawToken());
        assertNotEquals("raw-token", result.getNewRawToken());
    }

    @Test
    void validateAndRotate_lostConcurrentRotationRace_throwsAndDoesNotIssueANewToken() {
        // Simulates two concurrent /refresh calls presenting the same still-valid
        // token: both pass the findByTokenHash check (revokedAt == null at read
        // time), but only one revokeByTokenHash can actually flip the guarded
        // row (WHERE revoked_at IS NULL) — that one returns 1, the loser gets 0.
        // The loser must fail closed, not also mint a sibling session.
        RefreshToken active = new RefreshToken(1, "s@example.test", 1, "hash",
                Timestamp.from(Instant.now().plusSeconds(600)), null, Timestamp.from(Instant.now()));
        when(refreshTokenRepository.findByTokenHash(anyString())).thenReturn(active);
        when(refreshTokenRepository.revokeByTokenHash(anyString())).thenReturn(0);

        assertThrows(UnauthorizedException.class, () -> refreshTokenService.validateAndRotate("raw-token"));

        verify(refreshTokenRepository, never()).insertRefreshToken(anyString(), any(), anyString(), any());
    }

    @Test
    void revoke_blankToken_doesNothing() {
        refreshTokenService.revoke("");
        refreshTokenService.revoke(null);

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void revoke_realToken_revokesByItsHash() {
        refreshTokenService.revoke("some-raw-token");

        verify(refreshTokenRepository).revokeByTokenHash(anyString());
    }
}
