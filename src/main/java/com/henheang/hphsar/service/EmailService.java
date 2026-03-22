package com.henheang.hphsar.service;

public interface EmailService {
    void sendSimpleMail(String recipient, String msgBody, String subject);

//    String sendMailWithAttachment(Email email);
}
