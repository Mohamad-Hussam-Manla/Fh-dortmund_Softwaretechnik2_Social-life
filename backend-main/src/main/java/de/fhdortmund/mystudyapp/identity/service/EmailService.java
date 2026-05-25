package de.fhdortmund.mystudyapp.identity.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:noreply@mystudyapp.de}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your MyStudyApp Account");
        message.setText(
            "Welcome to MyStudyApp!\n\n" +
            "Please click the link below to verify your account. " +
            "This link expires in 24 hours.\n\n" +
            verificationUrl + "\n\n" +
            "If you did not create an account, you can safely ignore this email."
        );

        mailSender.send(message);
        log.info("Verification email sent to {}", toEmail);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your MyStudyApp Password");
        message.setText(
            "Hi,\n\n" +
            "We received a request to reset your MyStudyApp password.\n\n" +
            "Click the link below to set a new password. " +
            "This link expires in 1 hour.\n\n" +
            resetUrl + "\n\n" +
            "If you did not request a password reset, you can safely ignore this email. " +
            "Your password will not change."
        );

        mailSender.send(message);
        log.info("Password reset email sent to {}", toEmail);
    }
}