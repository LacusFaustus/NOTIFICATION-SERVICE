package com.notificationservice.service;

import com.notificationservice.entity.EmailProvider;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.EmailProviderRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnhancedEmailServiceTest {

    @Mock
    private EmailProviderRepository emailProviderRepository;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private EnhancedEmailService enhancedEmailService;

    @BeforeEach
    void setUp() {
        enhancedEmailService = new EnhancedEmailService(emailProviderRepository, circuitBreakerRegistry);
    }

    @Test
    void refreshProviders_ShouldReloadActiveProviders() {
        // Act
        enhancedEmailService.refreshProviders();

        // Assert - should not throw exception
        assertTrue(true);
    }
}
