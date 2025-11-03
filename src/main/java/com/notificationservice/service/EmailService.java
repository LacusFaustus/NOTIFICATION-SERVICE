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

    @Value("${notification.email.test-mode:false}")
    private boolean testMode;

    public void sendEmail(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }

        try {
            if (testMode) {
                // В тестовом режиме только логируем, но считаем успешной отправку
                log.info("📧 [TEST MODE] Mock email sent to: {} with subject: {}",
                        notification.getRecipient(), notification.getSubject());
                log.debug("📧 [TEST MODE] Email content: {}", notification.getMessage());
                // В тестовом режиме записываем метрики для тестирования
                metricsService.recordEmailSent();
                return;
            }

            // Реальная отправка email
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

    public void sendEmailWithTemplate(String to, String subject, String templateId, Object variables) {
        if (testMode) {
            log.info("📧 [TEST MODE] Mock template email sent to: {} with template: {}", to, templateId);
            log.debug("📧 [TEST MODE] Subject: {}, Variables: {}", subject, variables);
            return;
        }

        // Реализация для продакшн режима
        log.info("Sending template email to: {}, template: {}, subject: {}", to, templateId, subject);
        // Здесь будет реальная логика отправки email с шаблоном
    }
}
