package com.notificationservice.controller;

import com.notificationservice.config.TestSecurityConfig;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.dto.PushRequest;
import com.notificationservice.entity.Notification;
import com.notificationservice.exception.NotificationNotFoundException;
import com.notificationservice.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void sendEmail_WithValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setTo("test@example.com");
        emailRequest.setSubject("Test Subject");
        emailRequest.setMessage("Test Message");

        NotificationResponse response = NotificationResponse.success(
                "test-id", "EMAIL", "test@example.com", "Test Subject", "Test Message"
        );

        when(notificationService.sendEmail(any(EmailRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emailRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipient").value("test@example.com"))
                .andExpect(jsonPath("$.subject").value("Test Subject"))
                .andExpect(jsonPath("$.message").value("Test Message"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void sendPush_WithValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        PushRequest pushRequest = new PushRequest();
        pushRequest.setUserId("user-123");
        pushRequest.setTitle("Test Title");
        pushRequest.setMessage("Test Message");
        pushRequest.setPlatform("IOS");

        NotificationResponse response = NotificationResponse.success(
                "test-id", "PUSH", "user-123", "Test Title", "Test Message"
        );

        when(notificationService.sendPush(any(PushRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pushRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.type").value("PUSH"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipient").value("user-123"))
                .andExpect(jsonPath("$.subject").value("Test Title"))
                .andExpect(jsonPath("$.message").value("Test Message"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getNotificationStatus_WithExistingId_ShouldReturnOk() throws Exception {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setType("EMAIL");
        notification.setStatus("SENT");
        notification.setRecipient("test@example.com");
        notification.setSubject("Test Subject");
        notification.setMessage("Test Message");
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationService.getNotificationStatus(notificationId)).thenReturn(notification);

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notificationId))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipient").value("test@example.com"));
    }

    @Test
    void getNotificationStatus_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        String notificationId = "non-existing-id";
        when(notificationService.getNotificationStatus(notificationId))
                .thenThrow(new NotificationNotFoundException("Notification not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Notification Not Found"))
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }

    @Test
    void sendEmail_WithFailedResponse_ShouldReturnOkWithError() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // Используем метод failed вместо error
        NotificationResponse response = NotificationResponse.failed(
                "test-id", "EMAIL", "test@example.com", "Test Subject", "SMTP error"
        );

        when(notificationService.sendEmail(any(EmailRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("SMTP error"));
    }

    @Test
    void sendEmail_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Arrange
        EmailRequest invalidRequest = new EmailRequest();
        invalidRequest.setTo(""); // Invalid email
        invalidRequest.setSubject(""); // Empty subject
        invalidRequest.setMessage(""); // Empty message

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendEmail(any());
    }
}
