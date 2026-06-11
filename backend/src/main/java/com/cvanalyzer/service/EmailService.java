package com.cvanalyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@cvanalyzer.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Async
    public void sendEmailVerification(String toEmail, String fullName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Verify your CV Analyzer account");
            message.setText(
                    "Hello " + fullName + ",\n\n" +
                    "Thank you for registering with CV Analyzer!\n\n" +
                    "Please verify your email address by clicking the link below:\n" +
                    frontendUrl + "/verify-email?token=" + token + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you did not create an account, please ignore this email.\n\n" +
                    "Best regards,\nCV Analyzer Team"
            );
            mailSender.send(message);
            log.info("Email verification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", toEmail, e);
        }
    }

    @Async
    public void sendPasswordReset(String toEmail, String fullName, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Reset your CV Analyzer password");
            message.setText(
                    "Hello " + fullName + ",\n\n" +
                    "We received a request to reset your password.\n\n" +
                    "Click the link below to reset your password:\n" +
                    frontendUrl + "/reset-password?token=" + token + "\n\n" +
                    "This link will expire in 1 hour.\n\n" +
                    "If you did not request a password reset, please ignore this email.\n\n" +
                    "Best regards,\nCV Analyzer Team"
            );
            mailSender.send(message);
            log.info("Password reset email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send password reset email to: {}", toEmail, e);
        }
    }

    @Async
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to CV Analyzer!");
            message.setText(
                    "Hello " + fullName + ",\n\n" +
                    "Welcome to CV Analyzer! Your account has been verified successfully.\n\n" +
                    "You can now:\n" +
                    "• Upload your resume for AI-powered analysis\n" +
                    "• Get ATS score and improvement suggestions\n" +
                    "• Generate an improved version of your resume\n" +
                    "• Chat with our AI career coach\n\n" +
                    "Get started at: " + frontendUrl + "\n\n" +
                    "Best regards,\nCV Analyzer Team"
            );
            mailSender.send(message);
            log.info("Welcome email sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send welcome email to: {}", toEmail, e);
        }
    }
}
