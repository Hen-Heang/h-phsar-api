package com.henheang.stock_flow_commerce.service.support;

import com.henheang.stock_flow_commerce.model.appUser.AppUser;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserProvider {

    public AppUser getCurrentUser() {
        return (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }
}
