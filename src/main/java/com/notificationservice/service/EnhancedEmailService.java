package com.notificationservice.service;

import com.notificationservice.entity.EmailProvider;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.EmailProviderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedEmailService {

    private final EmailProviderRepository emailProviderRepository;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private final AtomicInteger currentProviderIndex = new AtomicInteger(0);
    private List<EmailProvider> activeProviders;

    public void sendEmailWithFailover(Notification notification) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("emailService");

        circuitBreaker.executeRunnable(() -> {
            boolean sent = false;
            int attempts = 0;
            int maxAttempts = getActiveProviders().size();

            while (!sent && attempts < maxAttempts) {
                EmailProvider provider = getNextProvider();
                try {
                    sendEmailWithProvider(notification, provider);
                    sent = true;
                    log.info("Email sent successfully using provider: {}", provider.getName());
                } catch (Exception e) {
                    attempts++;
                    log.warn("Failed to send email with provider {}: {}", provider.getName(), e.getMessage());
                    // Mark provider as temporarily unavailable
                    provider.setActive(false);
                    emailProviderRepository.save(provider);
                }
            }

            if (!sent) {
                throw new RuntimeException("All email providers failed");
            }
        });
    }

    private void sendEmailWithProvider(Notification notification, EmailProvider provider) {
        JavaMailSender mailSender = createMailSender(provider);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            var helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getRecipient());
            helper.setSubject(notification.getSubject());
            helper.setText(notification.getMessage(), true);
            helper.setFrom(provider.getFromEmail());

            mailSender.send(message);

        } catch (Exception e) {
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

        return mailSender;
    }

    private synchronized List<EmailProvider> getActiveProviders() {
        if (activeProviders == null) {
            activeProviders = emailProviderRepository.findByActiveTrue();
        }
        return activeProviders;
    }

    private EmailProvider getNextProvider() {
        List<EmailProvider> providers = getActiveProviders();
        if (providers.isEmpty()) {
            throw new RuntimeException("No active email providers available");
        }

        int index = currentProviderIndex.getAndUpdate(i -> (i + 1) % providers.size());
        return providers.get(index);
    }

    public void refreshProviders() {
        this.activeProviders = null;
        getActiveProviders(); // Reload providers
    }
}
