package com.henheang.hphsar.model.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a successful refresh-token rotation: the account whose session
 * was refreshed, plus the new raw refresh token to send back as a cookie.
 * The caller (controller) uses email to regenerate a new access token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshedSession {
    private String email;
    private String newRawToken;
}
