package com.henheang.stock_flow_commerce.service;

public interface EmailService {
    void sendSimpleMail(String recipient, String msgBody, String subject);

//    String sendMailWithAttachment(Email email);
}
