package com.furniture.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {
    
    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;
    
    @Value("${sendgrid.from.email}")
    private String fromEmail;
    
    @Value("${sendgrid.from.name}")
    private String fromName;

    public void sendVerificationOtpEmail(@NonNull String userEmail, @NonNull String otp, @NonNull String subject, @NonNull String text) {
        try {
            log.info("Attempting to send email via SendGrid to: {}", userEmail);
            
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(userEmail);
            Content content = new Content("text/html", text);
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Email sent successfully to: {} (Status: {})", userEmail, response.getStatusCode());
            } else {
                log.error("Failed to send email to {}: Status {}, Body: {}", 
                         userEmail, response.getStatusCode(), response.getBody());
            }
            
        } catch (IOException e) {
            log.error("Failed to send email to {}: {}", userEmail, e.getMessage(), e);
        }
    }
}
