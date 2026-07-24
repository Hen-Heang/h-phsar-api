package com.henheang.hphsar.repository;

import com.henheang.hphsar.model.jwt.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;

/**
 * RefreshTokenRepository — refresh-token persistence.
 * <p>
 * All SQL lives in resources/mapper/RefreshTokenMapper.xml, matched by method
 * name (MyBatis XML convention — see AppUserRepository for the full explanation).
 */
@Mapper
public interface RefreshTokenRepository {

    RefreshToken insertRefreshToken(@Param("email") String email,
                                     @Param("roleId") Integer roleId,
                                     @Param("tokenHash") String tokenHash,
                                     @Param("expiresAt") Timestamp expiresAt);

    RefreshToken findByTokenHash(@Param("tokenHash") String tokenHash);

    // Guarded UPDATE (WHERE ... revoked_at IS NULL) — returns affected-row count
    // so the service layer can tell "revoked now" apart from "was already revoked".
    Integer revokeByTokenHash(@Param("tokenHash") String tokenHash);

    // Used for stolen-token breach response: kill every other active session
    // for this account when a rotated-away token is presented again.
    Integer revokeAllActiveByEmail(@Param("email") String email);
}
