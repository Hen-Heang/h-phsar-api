package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.practice.CurrentUserResponse;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PracticeServiceImplTest {

    @Test
    void getCurrentUserReturnsSafeAuthenticatedUserDetails() {
        AppUser currentUser = new AppUser(7, "buyer@example.com", "secret-hash", null, 2, true, true);
        CurrentUserProvider currentUserProvider = new CurrentUserProvider() {
            @Override
            public AppUser getCurrentUser() {
                return currentUser;
            }
        };

        PracticeServiceImpl practiceService = new PracticeServiceImpl(currentUserProvider);

        CurrentUserResponse response = practiceService.getCurrentUser();

        assertThat(response.id()).isEqualTo(7);
        assertThat(response.email()).isEqualTo("buyer@example.com");
        assertThat(response.roleId()).isEqualTo(2);
        assertThat(response.role()).isEqualTo("BUYER");

    }
}
