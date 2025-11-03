package com.notificationservice.integration;

import com.notificationservice.config.TestApplicationConfig;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.service.EmailService;
import com.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApplicationConfig.class)
@ActiveProfiles("test")
class AsyncNotificationTest {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private EmailService emailService;

    @Test
    void sendEmail_ShouldCompleteSuccessfully() {
        // Given
        EmailRequest request = createValidEmailRequest();

        // Configure mock for void method
        doNothing().when(emailService).sendEmail(any());

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());
        assertEquals("SENT", response.getStatus());
    }

    @Test
    void processHighVolumeNotifications_ShouldHandleMultipleRequests() {
        // Given
        EmailRequest request = createValidEmailRequest();
        doNothing().when(emailService).sendEmail(any());

        // When - отправляем несколько уведомлений
        for (int i = 0; i < 3; i++) {
            NotificationResponse response = notificationService.sendEmail(request);
            assertNotNull(response);
            assertTrue(response.isSuccess());
        }

        // Then - проверяем что все вызовы обработаны
        verify(emailService, times(3)).sendEmail(any());
    }

    @Test
    void sendEmailAsync_ShouldCompleteSuccessfully() {
        // Given
        EmailRequest request = createValidEmailRequest();
        doNothing().when(emailService).sendEmail(any());

        // When
        CompletableFuture<NotificationResponse> future = notificationService.sendEmailAsync(request);

        // Then
        await().atMost(5, TimeUnit.SECONDS).until(future::isDone);
        assertTrue(future.isDone());
        assertFalse(future.isCompletedExceptionally());

        NotificationResponse response = future.join();
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertEquals("SENT", response.getStatus());
    }

    @Test
    void sendEmail_WhenEmailServiceFails_ShouldReturnFailedResponse() {
        // Given
        EmailRequest request = createValidEmailRequest();
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail(any());

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
        assertEquals("FAILED", response.getStatus());
    }

    private EmailRequest createValidEmailRequest() {
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message");
        return request;
    }
}
