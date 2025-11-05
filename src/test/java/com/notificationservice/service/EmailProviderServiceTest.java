package com.notificationservice.service;

import com.notificationservice.entity.EmailProvider;
import com.notificationservice.repository.EmailProviderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailProviderServiceTest {

    @Mock
    private EmailProviderRepository emailProviderRepository;

    @InjectMocks
    private EmailProviderService emailProviderService;

    @Test
    void testConnection_WithValidProvider_ShouldReturnTrue() {
        // Arrange
        EmailProvider provider = createValidEmailProvider();

        // Act
        boolean result = emailProviderService.testConnection(provider);

        // Assert
        assertTrue(result, "Connection test should pass for valid provider");
    }

    @Test
    void testConnection_WithInvalidProvider_ShouldReturnFalse() {
        // Arrange
        EmailProvider provider = new EmailProvider();
        provider.setHost("invalid-host-that-does-not-exist-12345.com");
        provider.setPort(9999);
        provider.setUsername("test");
        provider.setPassword("password");

        // Act
        boolean result = emailProviderService.testConnection(provider);

        // Assert
        assertFalse(result, "Connection test should fail for invalid provider");
    }

    @Test
    void testConnection_WithNullProvider_ShouldReturnFalse() {
        // Act
        boolean result = emailProviderService.testConnection(null);

        // Assert
        assertFalse(result, "Connection test should fail for null provider");
    }

    @Test
    void testConnection_WithEmptyHost_ShouldReturnFalse() {
        // Arrange
        EmailProvider provider = createValidEmailProvider();
        provider.setHost("");

        // Act
        boolean result = emailProviderService.testConnection(provider);

        // Assert
        assertFalse(result, "Connection test should fail for empty host");
    }

    @Test
    void testConnection_WithInvalidPort_ShouldReturnFalse() {
        // Arrange
        EmailProvider provider = createValidEmailProvider();
        provider.setPort(0); // Invalid port

        // Act
        boolean result = emailProviderService.testConnection(provider);

        // Assert
        assertFalse(result, "Connection test should fail for invalid port");
    }

    @Test
    void checkAllProvidersHealth_ShouldNotThrowException() {
        // Arrange
        EmailProvider healthyProvider = createValidEmailProvider();
        healthyProvider.setId("healthy-1");
        healthyProvider.setName("Healthy Provider");

        when(emailProviderRepository.findByActiveTrue())
                .thenReturn(List.of(healthyProvider));

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            emailProviderService.checkAllProvidersHealth();
        });

        // Verify repository was called
        verify(emailProviderRepository, times(1)).findByActiveTrue();
    }

    @Test
    void checkAllProvidersHealth_WithEmptyList_ShouldNotThrowException() {
        // Arrange
        when(emailProviderRepository.findByActiveTrue())
                .thenReturn(List.of());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            emailProviderService.checkAllProvidersHealth();
        });

        // Verify repository was called
        verify(emailProviderRepository, times(1)).findByActiveTrue();
    }

    private EmailProvider createValidEmailProvider() {
        EmailProvider provider = new EmailProvider();
        provider.setHost("smtp.gmail.com");
        provider.setPort(587);
        provider.setUsername("test@example.com");
        provider.setPassword("password");
        provider.setFromEmail("noreply@example.com");
        provider.setFromName("Test Service");
        provider.setUseTls(true);
        provider.setConnectionTimeout(5000);
        provider.setTimeout(5000);
        return provider;
    }
}
