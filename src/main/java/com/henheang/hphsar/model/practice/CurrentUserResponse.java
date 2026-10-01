package com.henheang.hphsar.model.practice;

/**
 * Safe view of the authenticated user. Deliberately excludes the password hash and account flags.
 *
 * @param id     user ID
 * @param email  login email
 * @param roleId role ID stored on the user
 * @param role   role name derived from the granted authority ({@code SUPPLIER}, {@code BUYER}, {@code ADMIN})
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
public record CurrentUserResponse(
        Integer id,
        String email,
        Integer roleId,
        String role
) {
}
