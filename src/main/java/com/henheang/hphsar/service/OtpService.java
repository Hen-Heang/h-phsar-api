package com.henheang.hphsar.service;

public interface OtpService {
    String generateOtp(String email);
    String verifyOtp(Integer otp, String email);

}
