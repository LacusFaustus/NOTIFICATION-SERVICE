package com.notificationservice.service;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.PushRequest;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationMetricsTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PushService pushService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private TemplateService templateService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void whenEmailSentSuccessfully_ShouldRecordMetrics() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test");
        request.setMessage("Test message");

        String notificationId = UUID.randomUUID().toString();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(notificationId);
            return notification;
        });

        doNothing().when(emailService).sendEmail(any(Notification.class));

        // When
        notificationService.sendEmail(request);

        // Then
        verify(metricsService, times(1)).recordEmailSent();
        verify(metricsService, never()).recordEmailFailed();
    }

    @Test
    void whenPushSentSuccessfully_ShouldRecordMetrics() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user123");
        request.setTitle("Test");
        request.setMessage("Test message");

        String notificationId = UUID.randomUUID().toString();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(notificationId);
            return notification;
        });

        doNothing().when(pushService).sendPush(any(Notification.class));

        // When
        notificationService.sendPush(request);

        // Then
        verify(metricsService, times(1)).recordPushSent();
        verify(metricsService, never()).recordPushFailed();
    }

    @Test
    void whenEmailValidationFails_ShouldRecordFailureMetrics() {
        // Given
        EmailRequest invalidRequest = new EmailRequest();
        invalidRequest.setTo(""); // Невалидный email
        invalidRequest.setSubject("Test");
        invalidRequest.setMessage("Test message");

        // When
        notificationService.sendEmail(invalidRequest);

        // Then
        verify(metricsService, times(1)).recordEmailFailed();
        verify(metricsService, never()).recordEmailSent();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void whenPushValidationFails_ShouldRecordFailureMetrics() {
        // Given
        PushRequest invalidRequest = new PushRequest();
        invalidRequest.setUserId(""); // Невалидный userId
        invalidRequest.setTitle("Test");
        invalidRequest.setMessage("Test message");

        // When
        notificationService.sendPush(invalidRequest);

        // Then
        verify(metricsService, times(1)).recordPushFailed();
        verify(metricsService, never()).recordPushSent();
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void whenEmailSendingFails_ShouldRecordFailureMetrics() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test");
        request.setMessage("Test message");

        String notificationId = UUID.randomUUID().toString();

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId(notificationId);
            return notification;
        });

        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailService).sendEmail(any(Notification.class));

        // When
        notificationService.sendEmail(request);

        // Then
        verify(metricsService, times(1)).recordEmailFailed();
        verify(metricsService, never()).recordEmailSent();
    }
}
