package com.notificationservice.service;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationStatusTest {

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

    @Test
    void sendEmail_ShouldUpdateStatusToSent_WhenEmailSentSuccessfully() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // Mock first save (PENDING status)
        Notification pendingNotification = createEmailNotification(request);
        pendingNotification.setId("test-id");
        pendingNotification.setStatus("PENDING");

        // Mock second save (SENT status)
        Notification sentNotification = createEmailNotification(request);
        sentNotification.setId("test-id");
        sentNotification.setStatus("SENT");
        sentNotification.setSentAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(pendingNotification)  // First call returns PENDING
                .thenReturn(sentNotification);    // Second call returns SENT

        // Act
        NotificationResponse response = notificationService.sendEmail(request);

        // Assert
        assertNotNull(response);
        assertEquals("SENT", response.getStatus());
        assertTrue(response.isSuccess());

        // Verify that save was called twice: first with PENDING, then with SENT
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailSent();
    }

    @Test
    void sendEmail_ShouldUpdateStatusToFailed_WhenEmailSendingFails() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // Mock first save (PENDING status)
        Notification pendingNotification = createEmailNotification(request);
        pendingNotification.setId("test-id");
        pendingNotification.setStatus("PENDING");

        // Mock second save (FAILED status)
        Notification failedNotification = createEmailNotification(request);
        failedNotification.setId("test-id");
        failedNotification.setStatus("FAILED");
        failedNotification.setErrorMessage("SMTP error");

        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(pendingNotification)  // First call returns PENDING
                .thenReturn(failedNotification);  // Second call returns FAILED

        // Mock email service to throw exception
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(any(Notification.class));

        // Act
        NotificationResponse response = notificationService.sendEmail(request);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailFailed();
    }

    private Notification createEmailNotification(EmailRequest request) {
        Notification notification = new Notification();
        notification.setType("EMAIL");
        notification.setStatus("PENDING");
        notification.setRecipient(request.getTo());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
