package com.henheang.stock_flow_commerce.service;

public interface OtpService {
    String generateOtp(String email);
    String verifyOtp(Integer otp, String email);

}
