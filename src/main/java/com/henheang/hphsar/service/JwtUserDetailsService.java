package com.henheang.hphsar.service;


import com.henheang.hphsar.model.appUser.AppUserDto;
import com.henheang.hphsar.model.appUser.AppUserRequest;
import com.henheang.hphsar.model.appUser.LoginResponse;
import com.henheang.hphsar.model.jwt.JwtChangePasswordRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtUserDetailsService{
    AppUserDto insertUser(AppUserRequest appUserRequest);

    boolean getVerifyEmail(String email);

    AppUserDto changePassword(JwtChangePasswordRequest request);

    String forgetPassword(Integer otp, String email, String newPassword);

    /**
     * Verifies login credentials.
     * If the email isn't verified yet, triggers a new OTP and rejects with {@link com.henheang.hphsar.exception.ConflictException}.
     * Otherwise delegates to Spring Security and translates its exceptions into this app's domain exceptions.
     */
    void authenticateLogin(String email, String password) throws Exception;
}
