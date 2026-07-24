package com.henheang.hphsar.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * RefreshToken — persisted row backing the refresh-token cookie flow.
 * <p>
 * Only the SHA-256 hash of the raw token is ever stored (tokenHash) — the raw
 * value only ever exists in the Set-Cookie response and the client's cookie
 * jar, never in the database or logs.
 * <p>
 * email + roleId (not a generic userId) identify the account: accounts live
 * in three separate tables (tb_supplier_account / tb_buyer_account /
 * tb_admin_account) with no shared id space, and email is what
 * JwtUserDetailsServiceImpl#loadUserByUsername already keys off of.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private Integer id;
    private String email;
    private Integer roleId;
    private String tokenHash;
    private Timestamp expiresAt;
    private Timestamp revokedAt;
    private Timestamp createdDate;
}
