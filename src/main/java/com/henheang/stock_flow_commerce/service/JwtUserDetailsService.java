package com.henheang.stock_flow_commerce.service;


import com.henheang.stock_flow_commerce.model.appUser.AppUserDto;
import com.henheang.stock_flow_commerce.model.appUser.AppUserRequest;
import com.henheang.stock_flow_commerce.model.appUser.LoginResponse;
import com.henheang.stock_flow_commerce.model.jwt.JwtChangePasswordRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtUserDetailsService{
    AppUserDto insertUser(AppUserRequest appUserRequest);

    boolean getVerifyEmail(String email);

    AppUserDto changePassword(JwtChangePasswordRequest request);

    String forgetPassword(Integer otp, String email, String newPassword);
}
