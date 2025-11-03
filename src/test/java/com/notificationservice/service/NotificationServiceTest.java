package com.notificationservice.service;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private NotificationService notificationService;

    private EmailRequest validEmailRequest;

    @BeforeEach
    void setUp() {
        validEmailRequest = new EmailRequest();
        validEmailRequest.setTo("test@example.com");
        validEmailRequest.setSubject("Test Subject");
        validEmailRequest.setMessage("Test Message");
    }

    @Test
    void sendEmail_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        // Act
        NotificationResponse response = notificationService.sendEmail(validEmailRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test-id", response.getId());
        assertEquals("EMAIL", response.getType());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals("SENT", response.getStatus());
        assertTrue(response.isSuccess());

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailSent();
    }

    @Test
    void sendEmail_WithInvalidRequest_ShouldReturnFailedResponse() {
        // Arrange
        EmailRequest invalidRequest = new EmailRequest();
        invalidRequest.setTo("");
        invalidRequest.setSubject("");
        invalidRequest.setMessage("");

        // Act
        NotificationResponse response = notificationService.sendEmail(invalidRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(emailService, never()).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailFailed();
    }
}
