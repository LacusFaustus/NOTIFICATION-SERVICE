package com.notificationservice.validation;

import com.notificationservice.config.TestSecurityConfig;
import com.notificationservice.controller.NotificationController;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
class NotificationValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @Test
    void whenValidEmailRequest_ShouldReturnOk() throws Exception {
        NotificationResponse response = NotificationResponse.success(
                "test-id", "EMAIL", "test@example.com", "Test", "Test"
        );
        when(notificationService.sendEmail(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "to": "valid@example.com",
                        "subject": "Test Subject",
                        "message": "Test Message",
                        "priority": "NORMAL"
                    }
                    """))
                .andExpect(status().isOk());
    }

    @Test
    void whenValidPushRequest_ShouldReturnOk() throws Exception {
        NotificationResponse response = NotificationResponse.success(
                "test-id", "PUSH", "user123", "Test", "Test"
        );
        when(notificationService.sendPush(org.mockito.ArgumentMatchers.any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "userId": "user123",
                        "title": "Test Title",
                        "message": "Test Message",
                        "platform": "IOS",
                        "priority": "NORMAL"
                    }
                    """))
                .andExpect(status().isOk());
    }
}
