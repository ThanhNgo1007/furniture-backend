package com.furniture.service;

import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



// Giả sử đây là file service, thêm @Async
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender javaMailSender;

    // @Async - Temporarily disabled for debugging
    public void sendVerificationOtpEmail(@NonNull String userEmail, @NonNull String otp, @NonNull String subject, @NonNull String text) throws MessagingException {
        try {
            log.info("Attempting to send email to: {}", userEmail);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setSubject(subject);
            helper.setText(text, true);
            helper.setTo(userEmail);
            javaMailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", userEmail);
        } catch (MailException e) {
            // Log error với stack trace đầy đủ
            log.error("Failed to send email to {}: {}", userEmail, e.getMessage(), e);
        }
    }
}
