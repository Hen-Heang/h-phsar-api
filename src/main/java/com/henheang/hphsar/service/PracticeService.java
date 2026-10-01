package com.henheang.hphsar.service;

import com.henheang.hphsar.model.practice.CurrentUserResponse;

/**
 * Practice service for reading the authenticated user.
 *
 * @author Hen-Heang
 * @since 2026.09.28
 * @version 1.0
 */
public interface PracticeService {

    /**
     * Returns the authenticated user's non-sensitive details from the security context.
     *
     * @return current user details
     */
    CurrentUserResponse getCurrentUser();
}
