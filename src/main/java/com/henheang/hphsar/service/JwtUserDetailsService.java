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
}
