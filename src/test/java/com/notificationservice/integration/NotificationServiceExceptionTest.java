package com.notificationservice.integration;

import com.notificationservice.config.TestApplicationConfig;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.exception.NotificationNotFoundException;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.service.EmailService;
import com.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = TestApplicationConfig.class)
@ActiveProfiles("test")
class NotificationServiceExceptionTest {

    @Autowired
    private NotificationService notificationService;

    @MockBean
    private NotificationRepository notificationRepository;

    @MockBean
    private EmailService emailService;

    @Test
    void getNotificationStatus_WhenNotFound_ShouldThrowException() {
        // Given
        String nonExistentId = UUID.randomUUID().toString();
        when(notificationRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NotificationNotFoundException.class, () -> {
            notificationService.getNotificationStatus(nonExistentId);
        });
    }

    @Test
    void sendEmail_WhenEmailServiceFails_ShouldHandleGracefully() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // Настраиваем мок для выброса исключения
        doThrow(new RuntimeException("SMTP error")).when(emailService).sendEmail(any());

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then - сервис должен обработать ошибку и вернуть корректный ответ
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());
    }
}
