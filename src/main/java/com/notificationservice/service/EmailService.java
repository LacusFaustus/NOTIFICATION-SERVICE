package com.notificationservice.service;

import com.notificationservice.entity.Notification;
import com.notificationservice.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final MetricsService metricsService;

    @Value("${notification.email.test-mode:true}")
    private boolean testMode;

    public void sendEmail(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }

        try {
            if (testMode) {
                log.info("📧 [TEST MODE] Mock email sent to: {} with subject: {}",
                        notification.getRecipient(), notification.getSubject());
                log.debug("📧 [TEST MODE] Email content: {}", notification.getMessage());
                metricsService.recordEmailSent();
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getMessage());

            mailSender.send(message);

            log.info("Email sent successfully to: {}", notification.getRecipient());
            metricsService.recordEmailSent();

        } catch (Exception e) {
            log.error("Failed to send email to: {}", notification.getRecipient(), e);
            metricsService.recordEmailFailed();
            throw new EmailSendingException("Email sending failed: " + e.getMessage(), e);
        }
    }
}
