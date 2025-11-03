package com.notificationservice.integration;

import com.notificationservice.config.TestSecurityConfig;
import com.notificationservice.controller.NotificationController;
import com.notificationservice.dto.EmailRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @Test
    void whenNotificationNotFound_ShouldReturnNotFoundResponse() throws Exception {
        // Arrange
        String notificationId = "non-existent-id";
        when(notificationService.getNotificationStatus(notificationId))
                .thenThrow(new NotificationNotFoundException("Notification not found with id: " + notificationId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/notifications/{id}", notificationId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Notification Not Found"))
                .andExpect(jsonPath("$.message").value("Notification not found with id: " + notificationId))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void whenInvalidEmailRequest_ShouldReturnValidationError() throws Exception {
        // Arrange
        EmailRequest invalidRequest = new EmailRequest();
        invalidRequest.setTo("invalid-email"); // Invalid email format
        invalidRequest.setSubject(""); // Empty subject
        invalidRequest.setMessage(""); // Empty message

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void whenServiceThrowsRuntimeException_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        when(notificationService.sendEmail(any(EmailRequest.class)))
                .thenThrow(new RuntimeException("Unexpected service error"));

        EmailRequest validRequest = new EmailRequest();
        validRequest.setTo("test@example.com");
        validRequest.setSubject("Test Subject");
        validRequest.setMessage("Test Message");

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void whenInvalidJsonInRequest_ShouldReturnBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenEmailSendingFails_ShouldReturnInternalServerError() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        when(notificationService.sendEmail(any(EmailRequest.class)))
                .thenThrow(new com.notificationservice.exception.EmailSendingException("SMTP server unavailable"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Email Sending Failed"))
                .andExpect(jsonPath("$.message").value("SMTP server unavailable"));
    }

    @Test
    void whenTemplateNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");
        request.setTemplateId("non-existent-template");

        when(notificationService.sendEmail(any(EmailRequest.class)))
                .thenThrow(new com.notificationservice.exception.TemplateNotFoundException("Template not found: non-existent-template"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Template Not Found"))
                .andExpect(jsonPath("$.message").value("Template not found: non-existent-template"));
    }
}
