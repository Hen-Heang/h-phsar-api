package com.henheang.hphsar.model.appUser;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure unit coverage of AppUser.getAuthorities() — no Spring context, no database.
 */
class AppUserTest {

    @Test
    void roleId1_grantsSupplierAuthority() {
        AppUser user = new AppUser(1, "a@a.com", "hash", null, 1, true, true);
        assertSingleAuthority(user, "SUPPLIER");
    }

    @Test
    void roleId2_grantsBuyerAuthority() {
        AppUser user = new AppUser(1, "a@a.com", "hash", null, 2, true, true);
        assertSingleAuthority(user, "BUYER");
    }

    @Test
    void roleId3_grantsAdminAuthority() {
        AppUser user = new AppUser(1, "a@a.com", "hash", null, 3, true, true);
        assertSingleAuthority(user, "ADMIN");
    }

    private void assertSingleAuthority(AppUser user, String expected) {
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals(expected, authorities.iterator().next().getAuthority());
    }
}
