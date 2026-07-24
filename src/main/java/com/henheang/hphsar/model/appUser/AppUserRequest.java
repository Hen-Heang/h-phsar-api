package com.henheang.hphsar.model.appUser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AppUserRequest — Registration Request Body
 * <p>
 * Received from the client when calling POST /authorization/register
 * <p>
 * Fields:
 *   email   → must be valid email format, must be unique
 *   password → plain text here, will be Bcrypt encoded before saving to DB
 *   roleId  → 1 = SUPPLIER, 2 = BUYER
 * <p>
 * FIX 9: Removed all unused imports (Spring Security + Jackson)
 * WHY: These were leftover from copy-paste. The class is just a plain
 *      data holder (DTO) and does not need to implement any interface.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUserRequest {
    private String email;
    private String password;
    private String confirmPassword;
    private Integer roleId;
}