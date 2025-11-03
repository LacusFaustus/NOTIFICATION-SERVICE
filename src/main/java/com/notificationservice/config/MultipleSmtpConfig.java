package com.notificationservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import com.notificationservice.service.EnhancedEmailService;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MultipleSmtpConfig {

    private final EnhancedEmailService enhancedEmailService;

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void refreshEmailProviders() {
        enhancedEmailService.refreshProviders();
    }
}
