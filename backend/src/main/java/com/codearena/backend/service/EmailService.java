package com.codearena.backend.service;

import com.codearena.backend.exception.EmailSendingException;

public interface EmailService {
    void sendSimpleMail(String to, String subject, String body) throws EmailSendingException;

    void sendHtmlMail(String to, String subject, String htmlContent) throws EmailSendingException;
}