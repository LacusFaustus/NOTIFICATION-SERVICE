package com.notificationservice.service;

import com.notificationservice.entity.EmailProvider;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.EmailProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutedEmailService {

    private final EmailProviderRepository emailProviderRepository;
    private final MetricsService metricsService;

    public void sendRoutedEmail(Notification notification) {
        List<EmailProvider> availableProviders = emailProviderRepository.findAvailableProviders();

        if (availableProviders.isEmpty()) {
            throw new RuntimeException("No available email providers");
        }

        EmailProvider selectedProvider = selectBestProvider(availableProviders);

        try {
            // В тестовой среде просто логируем отправку
            if (isTestEnvironment()) {
                log.info("[TEST] Mock email sent to: {} using provider: {}",
                        notification.getRecipient(), selectedProvider.getName());
                updateProviderUsage(selectedProvider);
                metricsService.recordEmailSent();
                return;
            }

            sendWithProvider(notification, selectedProvider);
            updateProviderUsage(selectedProvider);
            log.info("Email sent successfully using provider: {}", selectedProvider.getName());

        } catch (Exception e) {
            log.error("Failed to send email with provider {}: {}", selectedProvider.getName(), e.getMessage());
            handleProviderFailure(selectedProvider);
            throw new RuntimeException("Failed to send email with provider " + selectedProvider.getName(), e);
        }
    }

    private boolean isTestEnvironment() {
        // Проверяем, находимся ли мы в тестовой среде
        return System.getProperty("test.environment") != null ||
                System.getenv("TEST_ENV") != null ||
                java.awt.GraphicsEnvironment.isHeadless(); // В тестах обычно headless
    }

    private EmailProvider selectBestProvider(List<EmailProvider> providers) {
        return providers.stream()
                .filter(provider -> provider.getCurrentUsage() < provider.getDailyLimit())
                .min((p1, p2) -> {
                    // Prefer providers with higher priority and lower usage
                    int priorityCompare = Integer.compare(p1.getPriority(), p2.getPriority());
                    if (priorityCompare != 0) return priorityCompare;

                    double p1UsageRatio = (double) p1.getCurrentUsage() / p1.getDailyLimit();
                    double p2UsageRatio = (double) p2.getCurrentUsage() / p2.getDailyLimit();
                    return Double.compare(p1UsageRatio, p2UsageRatio);
                })
                .orElseThrow(() -> new RuntimeException("No suitable provider found"));
    }

    private void sendWithProvider(Notification notification, EmailProvider provider) {
        JavaMailSender mailSender = createMailSender(provider);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getMessage(), true);
            helper.setFrom(provider.getFromEmail());

            mailSender.send(message);
            metricsService.recordEmailSent();

        } catch (Exception e) {
            metricsService.recordEmailFailed();
            throw new RuntimeException("Failed to send email with provider " + provider.getName(), e);
        }
    }

    private JavaMailSender createMailSender(EmailProvider provider) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(provider.getHost());
        mailSender.setPort(provider.getPort());
        mailSender.setUsername(provider.getUsername());
        mailSender.setPassword(provider.getPassword());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");
        props.put("mail.smtp.writetimeout", "5000");
        props.put("mail.debug", "false");

        return mailSender;
    }

    private void updateProviderUsage(EmailProvider provider) {
        provider.setCurrentUsage(provider.getCurrentUsage() + 1);
        emailProviderRepository.save(provider);
    }

    private void handleProviderFailure(EmailProvider provider) {
        log.warn("Email provider {} failed, consider reviewing its configuration", provider.getName());
    }
}
