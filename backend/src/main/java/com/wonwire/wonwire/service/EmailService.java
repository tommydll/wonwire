package com.wonwire.wonwire.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Sends a password reset email with a link containing the reset token.
     * The link points to the frontend reset password page.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@wonwire.com");
        message.setTo(toEmail);
        message.setSubject("WonWire — Reset your password");
        message.setText("Hello,\n\n" +
                "You requested to reset your WonWire password.\n\n" +
                "Click the link below to reset it (valid for 15 minutes):\n" +
                resetLink + "\n\n" +
                "If you didn't request this, please ignore this email.\n\n" +
                "— The WonWire Team"
        );

        mailSender.send(message);
    }
}