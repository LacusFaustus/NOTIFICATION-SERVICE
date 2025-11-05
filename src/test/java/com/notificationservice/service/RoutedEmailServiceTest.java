package com.notificationservice.service;

import com.notificationservice.entity.EmailProvider;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.EmailProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutedEmailServiceTest {

    @Mock
    private EmailProviderRepository emailProviderRepository;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private RoutedEmailService routedEmailService;

    @Test
    void sendRoutedEmail_WithAvailableProviders_ShouldNotThrowException() {
        // Arrange
        Notification notification = createValidNotification();
        EmailProvider provider = createValidEmailProvider();

        when(emailProviderRepository.findAvailableProviders())
                .thenReturn(List.of(provider));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            routedEmailService.sendRoutedEmail(notification);
        });

        // Verify repository was called
        verify(emailProviderRepository, times(1)).findAvailableProviders();
        verify(metricsService, times(1)).recordEmailSent();
    }

    @Test
    void sendRoutedEmail_WithNoAvailableProviders_ShouldThrowException() {
        // Arrange
        Notification notification = createValidNotification();

        when(emailProviderRepository.findAvailableProviders())
                .thenReturn(List.of());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            routedEmailService.sendRoutedEmail(notification);
        });

        assertEquals("No available email providers", exception.getMessage());
        verify(emailProviderRepository, times(1)).findAvailableProviders();
    }

    @Test
    void sendRoutedEmail_WithMultipleProviders_ShouldSelectBestOne() {
        // Arrange
        Notification notification = createValidNotification();

        EmailProvider highPriorityProvider = createValidEmailProvider();
        highPriorityProvider.setId("provider-1");
        highPriorityProvider.setPriority(1);
        highPriorityProvider.setCurrentUsage(100);
        highPriorityProvider.setDailyLimit(1000);

        EmailProvider lowPriorityProvider = createValidEmailProvider();
        lowPriorityProvider.setId("provider-2");
        lowPriorityProvider.setPriority(2);
        lowPriorityProvider.setCurrentUsage(50);
        lowPriorityProvider.setDailyLimit(500);

        when(emailProviderRepository.findAvailableProviders())
                .thenReturn(List.of(highPriorityProvider, lowPriorityProvider));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            routedEmailService.sendRoutedEmail(notification);
        });

        // Verify repository was called
        verify(emailProviderRepository, times(1)).findAvailableProviders();
        verify(metricsService, times(1)).recordEmailSent();
    }

    private Notification createValidNotification() {
        Notification notification = new Notification();
        notification.setId("test-notification");
        notification.setRecipient("test@example.com");
        notification.setSubject("Test Subject");
        notification.setMessage("Test Message");
        notification.setType("EMAIL");
        return notification;
    }

    private EmailProvider createValidEmailProvider() {
        EmailProvider provider = new EmailProvider();
        provider.setId("test-provider");
        provider.setName("Test Provider");
        provider.setHost("smtp.test.com");
        provider.setPort(587);
        provider.setUsername("test-user");
        provider.setPassword("test-password");
        provider.setFromEmail("noreply@test.com");
        provider.setFromName("Test Service");
        provider.setActive(true);
        provider.setPriority(1);
        provider.setDailyLimit(1000);
        provider.setCurrentUsage(100);
        provider.setUseTls(true);
        return provider;
    }
}
