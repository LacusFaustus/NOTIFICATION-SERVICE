package com.notificationservice.service;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.PushRequest;
import com.notificationservice.entity.Notification;
import com.notificationservice.exception.NotificationNotFoundException;
import com.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceComprehensiveTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private PushService pushService;

    @Mock
    private TemplateService templateService;

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private NotificationService notificationService;

    private EmailRequest validEmailRequest;
    private PushRequest validPushRequest;

    @BeforeEach
    void setUp() {
        validEmailRequest = new EmailRequest();
        validEmailRequest.setTo("test@example.com");
        validEmailRequest.setSubject("Test Subject");
        validEmailRequest.setMessage("Test Message");

        validPushRequest = new PushRequest();
        validPushRequest.setUserId("user-123");
        validPushRequest.setTitle("Test Title");
        validPushRequest.setMessage("Test Message");
    }

    @Test
    void sendEmail_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        doNothing().when(emailService).sendEmail(any(Notification.class));

        // Act
        var response = notificationService.sendEmail(validEmailRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test-id", response.getId());
        assertEquals("EMAIL", response.getType());
        assertEquals("SENT", response.getStatus());
        assertTrue(response.isSuccess());

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailSent();
    }

    @Test
    void sendEmail_WithTemplate_ShouldProcessTemplate() {
        // Arrange
        validEmailRequest.setTemplateId("test-template");
        validEmailRequest.setTemplateVariables(Map.of("name", "John"));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        when(templateService.processTemplate("test-template", Map.of("name", "John")))
                .thenReturn("Processed template content");

        doNothing().when(emailService).sendEmail(any(Notification.class));

        // Act
        var response = notificationService.sendEmail(validEmailRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(templateService, times(1)).processTemplate("test-template", Map.of("name", "John"));
    }

    @Test
    void sendEmail_WithTemplateProcessingFailure_ShouldUseOriginalMessage() {
        // Arrange
        validEmailRequest.setTemplateId("invalid-template");
        validEmailRequest.setTemplateVariables(Map.of("name", "John"));

        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        when(templateService.processTemplate("invalid-template", Map.of("name", "John")))
                .thenThrow(new RuntimeException("Template not found"));

        doNothing().when(emailService).sendEmail(any(Notification.class));

        // Act
        var response = notificationService.sendEmail(validEmailRequest);

        // Assert
        assertNotNull(response);
        assertTrue(response.isSuccess());
        verify(templateService, times(1)).processTemplate("invalid-template", Map.of("name", "John"));
    }

    @Test
    void sendEmail_WithInvalidRequest_ShouldReturnFailedResponse() {
        // Arrange
        EmailRequest invalidRequest = new EmailRequest();
        invalidRequest.setTo("");
        invalidRequest.setSubject("");
        invalidRequest.setMessage("");

        // Act
        var response = notificationService.sendEmail(invalidRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(emailService, never()).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailFailed();
    }

    @Test
    void sendEmail_WhenEmailServiceFails_ShouldReturnFailedResponse() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(any(Notification.class));

        // Act
        var response = notificationService.sendEmail(validEmailRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(metricsService, times(1)).recordEmailFailed();
    }

    @Test
    void sendEmailAsync_ShouldReturnCompletableFuture() throws ExecutionException, InterruptedException {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        doNothing().when(emailService).sendEmail(any(Notification.class));

        // Act
        CompletableFuture<?> future = notificationService.sendEmailAsync(validEmailRequest);
        var response = future.get();

        // Assert
        assertNotNull(response);
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());
    }

    @Test
    void sendPush_WithValidRequest_ShouldReturnSuccessResponse() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        doNothing().when(pushService).sendPush(any(Notification.class));

        // Act
        var response = notificationService.sendPush(validPushRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test-id", response.getId());
        assertEquals("PUSH", response.getType());
        assertEquals("SENT", response.getStatus());
        assertTrue(response.isSuccess());

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(pushService, times(1)).sendPush(any(Notification.class));
        verify(metricsService, times(1)).recordPushSent();
    }

    @Test
    void sendPush_WithInvalidRequest_ShouldReturnFailedResponse() {
        // Arrange
        PushRequest invalidRequest = new PushRequest();
        invalidRequest.setUserId("");
        invalidRequest.setTitle("");
        invalidRequest.setMessage("");

        // Act
        var response = notificationService.sendPush(invalidRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        verify(notificationRepository, never()).save(any(Notification.class));
        verify(pushService, never()).sendPush(any(Notification.class));
        verify(metricsService, times(1)).recordPushFailed();
    }

    @Test
    void sendPush_WhenPushServiceFails_ShouldReturnFailedResponse() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            notification.setId("test-id");
            return notification;
        });

        doThrow(new RuntimeException("Push service error"))
                .when(pushService).sendPush(any(Notification.class));

        // Act
        var response = notificationService.sendPush(validPushRequest);

        // Assert
        assertNotNull(response);
        assertEquals("FAILED", response.getStatus());
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(pushService, times(1)).sendPush(any(Notification.class));
        verify(metricsService, times(1)).recordPushFailed();
    }

    @Test
    void getNotificationStatus_WithExistingId_ShouldReturnNotification() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("EMAIL");
        notification.setStatus("SENT");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        // Act
        var result = notificationService.getNotificationStatus(notificationId);

        // Assert
        assertNotNull(result);
        assertEquals(notificationId, result.getId());
        assertEquals("EMAIL", result.getType());
        assertEquals("SENT", result.getStatus());
    }

    @Test
    void getNotificationStatus_WithNonExistingId_ShouldThrowException() {
        // Arrange
        String notificationId = "non-existing-id";
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.getNotificationStatus(notificationId);
        });
    }

    @Test
    void processNotification_WithPendingEmail_ShouldProcessSuccessfully() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("EMAIL");
        notification.setStatus("PENDING");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        doNothing().when(emailService).sendEmail(any(Notification.class));

        // Act
        notificationService.processNotification(notificationId);

        // Assert
        verify(emailService, times(1)).sendEmail(any(Notification.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void processNotification_WithPendingPush_ShouldProcessSuccessfully() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("PUSH");
        notification.setStatus("PENDING");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        doNothing().when(pushService).sendPush(any(Notification.class));

        // Act
        notificationService.processNotification(notificationId);

        // Assert
        verify(pushService, times(1)).sendPush(any(Notification.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void processNotification_WithNonPendingStatus_ShouldSkipProcessing() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("EMAIL");
        notification.setStatus("SENT"); // Already sent

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        // Act
        notificationService.processNotification(notificationId);

        // Assert
        verify(emailService, never()).sendEmail(any(Notification.class));
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    void processNotification_WithUnknownType_ShouldThrowException() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("UNKNOWN");
        notification.setStatus("PENDING");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            notificationService.processNotification(notificationId);
        });
    }

    @Test
    void processNotification_WhenServiceFails_ShouldUpdateStatusToFailed() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("EMAIL");
        notification.setStatus("PENDING");

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        doThrow(new RuntimeException("Service error"))
                .when(emailService).sendEmail(any(Notification.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            notificationService.processNotification(notificationId);
        });

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}
