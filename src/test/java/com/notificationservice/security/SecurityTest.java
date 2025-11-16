package com.notificationservice.security;

import com.notificationservice.config.TestSecurityConfig;
import com.notificationservice.controller.NotificationController;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.service.NotificationService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void whenSendEmailNotification_ShouldBeAllowed() throws Exception {
        // Mock the service response
        NotificationResponse response = NotificationResponse.success(
                "test-id", "EMAIL", "test@example.com", "Test Subject", "Test message"
        );
        when(notificationService.sendEmail(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "to": "test@example.com",
                                "subject": "Test Subject",
                                "message": "Test message content",
                                "priority": "NORMAL"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void whenGetNotificationStatus_ShouldBeAllowed() throws Exception {
        // Mock the service response
        Notification notification = new Notification();
        notification.setId("test-id");
        notification.setType("EMAIL");
        notification.setStatus("SENT");
        notification.setRecipient("test@example.com");
        notification.setSubject("Test Subject");
        notification.setMessage("Test message");
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationService.getNotificationStatus("test-id")).thenReturn(notification);

        mockMvc.perform(get("/api/v1/notifications/{id}", "test-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test-id"))
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipient").value("test@example.com"));
    }

    @Test
    void whenSendMultipleEmailNotifications_ShouldWorkCorrectly() throws Exception {
        // Mock responses for multiple requests
        NotificationResponse response1 = NotificationResponse.success(
                "email-1", "EMAIL", "test1@example.com", "Test 1", "Message 1"
        );
        NotificationResponse response2 = NotificationResponse.success(
                "email-2", "EMAIL", "test2@example.com", "Test 2", "Message 2"
        );

        when(notificationService.sendEmail(any()))
                .thenReturn(response1)
                .thenReturn(response2);

        // First request
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "to": "test1@example.com",
                                "subject": "Test 1",
                                "message": "Message 1",
                                "priority": "NORMAL"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("email-1"));

        // Second request
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "to": "test2@example.com",
                                "subject": "Test 2",
                                "message": "Message 2",
                                "priority": "HIGH"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("email-2"));
    }

    @Test
    void whenSendEmailWithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "to": "invalid-email",
                                "subject": "",
                                "message": "",
                                "priority": "NORMAL"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenSendEmailWithMalformedJSON_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenSendEmailWithEmptyBody_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenGetNotificationWithInvalidId_ShouldReturnNotFound() throws Exception {
        when(notificationService.getNotificationStatus("non-existent-id"))
                .thenThrow(new com.notificationservice.exception.NotificationNotFoundException("Notification not found"));

        mockMvc.perform(get("/api/v1/notifications/{id}", "non-existent-id"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Notification Not Found"));
    }
}
