package com.notificationservice.service;

import com.notificationservice.config.TestApplicationConfig;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApplicationConfig.class)
@ActiveProfiles("test")
@Transactional
class NotificationServiceUnitTest {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private EmailService emailService;

    @Test
    void sendEmailSuccessfully() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");

        // Настраиваем mock
        doNothing().when(emailService).sendEmail(any());

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
    }

    @Test
    void getNotificationStatus() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message"); // Добавляем обязательное поле

        doNothing().when(emailService).sendEmail(any());

        // When
        NotificationResponse sentResponse = notificationService.sendEmail(request);

        // Then
        assertNotNull(sentResponse);
        assertNotNull(sentResponse.getId());

        // When
        var notification = notificationService.getNotificationStatus(sentResponse.getId());

        // Then
        assertNotNull(notification);
        assertEquals(sentResponse.getId(), notification.getId());
    }
}
