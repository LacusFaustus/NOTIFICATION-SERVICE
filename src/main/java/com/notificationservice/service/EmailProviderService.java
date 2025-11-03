package com.notificationservice.service;

import com.notificationservice.entity.EmailProvider;
import com.notificationservice.repository.EmailProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailProviderService {

    private final EmailProviderRepository emailProviderRepository;

    public boolean testConnection(EmailProvider provider) {
        try {
            JavaMailSender mailSender = createMailSender(provider);
            // Простая проверка соединения - попытка создания сессии
            ((JavaMailSenderImpl) mailSender).testConnection();
            return true;
        } catch (Exception e) {
            log.error("Connection test failed for provider {}: {}", provider.getName(), e.getMessage());
            return false;
        }
    }

    public void checkAllProvidersHealth() {
        List<EmailProvider> activeProviders = emailProviderRepository.findByActiveTrue();

        for (EmailProvider provider : activeProviders) {
            boolean isHealthy = testConnection(provider);
            if (!isHealthy) {
                log.warn("Email provider {} is unhealthy, deactivating", provider.getName());
                provider.setActive(false); // Исправлено на setActive
                emailProviderRepository.save(provider);
            }
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
}
