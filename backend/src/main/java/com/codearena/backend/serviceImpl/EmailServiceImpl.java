package com.codearena.backend.serviceImpl;

import com.codearena.backend.exception.EmailSendingException;
import com.codearena.backend.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendSimpleMail(String to, String subject, String body) throws EmailSendingException {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            logger.info("Email sent successfully to: {}", to);

        } catch (MailException e) {
            logger.error("Failed to send email to {}: {}", to, e.getMessage());
            throw new EmailSendingException("Failed to send email to " + to, e);

        } catch (Exception e) {
            logger.error("Unexpected error while sending email to {}: {}", to, e.getMessage());
            throw new EmailSendingException("Unexpected error while sending email", e);
        }
    }

    @Override
    public void sendHtmlMail(String to, String subject, String htmlContent) throws EmailSendingException {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML content

            mailSender.send(message);
            logger.info("HTML email sent successfully to: {}", to);

        } catch (Exception e) {
            logger.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new EmailSendingException("Failed to send HTML email to " + to, e);
        }
    }
}