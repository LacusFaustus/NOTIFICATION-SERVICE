package com.notificationservice.integration;

import com.notificationservice.NotificationServiceApplication;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.service.EmailService;
import com.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = NotificationServiceApplication.class)
@ActiveProfiles("test")
@Transactional
class NotificationServiceIT {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @SpyBean
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void sendEmail_WithRealDatabase_ShouldPersistData() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());

        // Проверяем, что уведомление сохранилось в базе данных
        Optional<Notification> savedNotification = notificationRepository.findById(response.getId());
        assertTrue(savedNotification.isPresent());

        Notification notification = savedNotification.get();
        assertEquals("test@example.com", notification.getRecipient());
        assertEquals("Test Subject", notification.getSubject());
        assertEquals("EMAIL", notification.getType());
        assertEquals("SENT", notification.getStatus()); // Статус должен быть SENT после успешной отправки
        assertNotNull(notification.getSentAt());

        // Проверяем, что emailService был вызван
        verify(emailService, times(1)).sendEmail(any(Notification.class));
    }

    @Test
    void sendEmail_WithInvalidRequest_ShouldReturnError() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo(""); // Невалидный email
        request.setSubject("Test Subject");
        request.setMessage("Test message");

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        // Проверяем, что запись в БД не создавалась
        assertEquals(0, notificationRepository.count());

        // Проверяем, что emailService НЕ был вызван
        verify(emailService, never()).sendEmail(any(Notification.class));
    }

    @Test
    void sendEmail_WithTemplate_ShouldPersistData() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("template@example.com");
        request.setSubject("Template Test");
        request.setMessage("Fallback message");
        request.setTemplateId("welcome-template");

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertNotNull(response.getId());
        assertTrue(response.isSuccess());

        // Проверяем сохранение в БД
        Optional<Notification> savedNotification = notificationRepository.findById(response.getId());
        assertTrue(savedNotification.isPresent());

        Notification notification = savedNotification.get();
        assertEquals("template@example.com", notification.getRecipient());
        assertEquals("EMAIL", notification.getType());
        assertEquals("SENT", notification.getStatus());
        assertEquals("welcome-template", notification.getTemplateId());

        verify(emailService, times(1)).sendEmail(any(Notification.class));
    }

    @Test
    void sendEmail_WhenEmailServiceFails_ShouldUpdateStatusToFailed() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message");

        // Настраиваем мок для выбрасывания исключения
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendEmail(any(Notification.class));

        // When
        NotificationResponse response = notificationService.sendEmail(request);

        // Then
        assertNotNull(response);
        assertFalse(response.isSuccess());
        assertNotNull(response.getErrorMessage());

        // Проверяем, что уведомление сохранилось с статусом FAILED
        Optional<Notification> savedNotification = notificationRepository.findById(response.getId());
        assertTrue(savedNotification.isPresent());

        Notification notification = savedNotification.get();
        assertEquals("FAILED", notification.getStatus());
        assertNotNull(notification.getErrorMessage());
        assertEquals("test@example.com", notification.getRecipient());
    }

    @Test
    void sendEmail_WithValidRequest_ShouldGenerateUniqueId() {
        // Given
        EmailRequest request1 = new EmailRequest();
        request1.setTo("test1@example.com");
        request1.setSubject("Test 1");
        request1.setMessage("Message 1");

        EmailRequest request2 = new EmailRequest();
        request2.setTo("test2@example.com");
        request2.setSubject("Test 2");
        request2.setMessage("Message 2");

        // When
        NotificationResponse response1 = notificationService.sendEmail(request1);
        NotificationResponse response2 = notificationService.sendEmail(request2);

        // Then
        assertNotNull(response1.getId());
        assertNotNull(response2.getId());
        assertNotEquals(response1.getId(), response2.getId());

        // Проверяем, что оба уведомления сохранились
        List<Notification> allNotifications = notificationRepository.findAll();
        assertEquals(2, allNotifications.size());

        // Проверяем, что оба имеют статус SENT
        assertEquals(2, allNotifications.stream()
                .filter(n -> "SENT".equals(n.getStatus()))
                .count());

        verify(emailService, times(2)).sendEmail(any(Notification.class));
    }
}
