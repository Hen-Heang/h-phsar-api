package com.henheang.hphsar.service.implement;

import com.henheang.hphsar.model.appUser.AppUser;
import com.henheang.hphsar.model.practice.CurrentUserResponse;
import com.henheang.hphsar.service.PracticeService;
import com.henheang.hphsar.service.support.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link PracticeService}.
 * <p>
 * The user always comes from {@link CurrentUserProvider}, never from request input.
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class PracticeServiceImpl implements PracticeService {

    private final CurrentUserProvider currentUserProvider;

    /** {@inheritDoc} */
    @Override
    public CurrentUserResponse getCurrentUser() {
        AppUser currentUser = currentUserProvider.getCurrentUser();
        String role = currentUser.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);

        return new CurrentUserResponse(
                currentUser.getId(),
                currentUser.getEmail(),
                currentUser.getRoleId(),
                role
        );
    }
}
