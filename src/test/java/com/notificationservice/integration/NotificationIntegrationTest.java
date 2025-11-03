package com.notificationservice.integration;

import com.notificationservice.NotificationServiceApplication;
import com.notificationservice.config.TestConfig;
import com.notificationservice.config.TestSecurityConfig;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.service.EmailService;
import com.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = NotificationServiceApplication.class)
@Import({TestConfig.class, TestSecurityConfig.class})
@ActiveProfiles("test")
@Transactional
class NotificationIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private EmailService emailService;

    @Test
    void sendEmail_WithValidRequest_ShouldPersistNotification() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");

        // НЕ настраиваем mock - пусть работает тестовый режим EmailService
        // В тестовом режиме EmailService сам логирует и вызывает metricsService.recordEmailSent()

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("SENT", response.getStatus());

        // Verify notification was saved in database
        Optional<Notification> savedNotification = notificationRepository.findById(response.getId());
        assertTrue(savedNotification.isPresent());

        Notification notification = savedNotification.get();
        assertEquals("test@example.com", notification.getRecipient());
        assertEquals("Test Subject", notification.getSubject());
        assertEquals("EMAIL", notification.getType());
        assertEquals("SENT", notification.getStatus());
    }

    @Test
    void sendEmail_WhenEmailServiceFails_ShouldReturnFailedStatus() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");

        // Настраиваем mock для выброса исключения
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(any(Notification.class));

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertFalse(response.isSuccess());
        assertEquals("FAILED", response.getStatus());
        assertNotNull(response.getErrorMessage());

        // Verify notification was saved with failed status
        Optional<Notification> savedNotification = notificationRepository.findById(response.getId());
        assertTrue(savedNotification.isPresent());
        assertEquals("FAILED", savedNotification.get().getStatus());
        assertEquals("SMTP error", savedNotification.get().getErrorMessage());
    }
}
