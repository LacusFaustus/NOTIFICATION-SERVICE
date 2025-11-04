package com.notificationservice.service;

import com.notificationservice.dto.BulkEmailRequest;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BulkNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private BulkNotificationService bulkNotificationService;

    @Test
    void sendBulkEmails_WithValidRequest_ShouldReturnResponses() {
        // Arrange
        EmailRequest emailRequest1 = new EmailRequest();
        emailRequest1.setTo("test1@example.com");
        emailRequest1.setSubject("Test 1");
        emailRequest1.setMessage("Message 1");

        EmailRequest emailRequest2 = new EmailRequest();
        emailRequest2.setTo("test2@example.com");
        emailRequest2.setSubject("Test 2");
        emailRequest2.setMessage("Message 2");

        BulkEmailRequest bulkRequest = new BulkEmailRequest();
        bulkRequest.setEmails(List.of(emailRequest1, emailRequest2));

        NotificationResponse response1 = NotificationResponse.success("id1", "EMAIL", "test1@example.com", "Test 1", "Success");
        NotificationResponse response2 = NotificationResponse.success("id2", "EMAIL", "test2@example.com", "Test 2", "Success");

        when(notificationService.sendEmail(any(EmailRequest.class)))
                .thenReturn(response1)
                .thenReturn(response2);

        // Act
        List<NotificationResponse> responses = bulkNotificationService.sendBulkEmails(bulkRequest);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("id1", responses.get(0).getId());
        assertEquals("id2", responses.get(1).getId());
    }

    @Test
    void sendBulkEmailsAsync_WithValidRequest_ShouldReturnCompletableFuture() {
        // Arrange
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo("test@example.com");
        emailRequest.setSubject("Test");
        emailRequest.setMessage("Message");

        BulkEmailRequest bulkRequest = new BulkEmailRequest();
        bulkRequest.setEmails(List.of(emailRequest));

        NotificationResponse response = NotificationResponse.success("id1", "EMAIL", "test@example.com", "Test", "Success");
        CompletableFuture<NotificationResponse> future = CompletableFuture.completedFuture(response);

        when(notificationService.sendEmailAsync(any(EmailRequest.class)))
                .thenReturn(future);

        // Act
        CompletableFuture<List<NotificationResponse>> resultFuture = bulkNotificationService.sendBulkEmailsAsync(bulkRequest);

        // Assert
        assertNotNull(resultFuture);
        assertDoesNotThrow(() -> {
            List<NotificationResponse> responses = resultFuture.get();
            assertNotNull(responses);
            assertEquals(1, responses.size());
        });
    }
}
