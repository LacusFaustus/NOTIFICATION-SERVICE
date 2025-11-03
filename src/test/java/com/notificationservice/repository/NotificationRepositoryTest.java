package com.notificationservice.repository;

import com.notificationservice.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    @Test
    void findByStatusAndCreatedAtBefore_ShouldRespectStatus() {
        // Given
        Notification pendingNotification = createNotification("pending@example.com", "Pending Subject", "Pending message");
        Notification sentNotification = createNotification("sent@example.com", "Sent Subject", "Sent message");
        sentNotification.setStatus("SENT");
        notificationRepository.save(sentNotification);

        // When - ищем только PENDING с временем в будущем (чтобы найти все)
        List<Notification> result = notificationRepository.findByStatusAndCreatedAtBefore("PENDING", LocalDateTime.now().plusHours(1));

        // Then - проверяем что нашли только PENDING запись
        assertEquals(1, result.size(), "Should find only PENDING notifications");
        assertEquals(pendingNotification.getId(), result.get(0).getId());
        assertEquals("PENDING", result.get(0).getStatus());
        assertEquals("pending@example.com", result.get(0).getRecipient());
    }

    @Test
    void findByStatusAndCreatedAtBefore_ShouldFindAllWhenTimeIsInFuture() {
        // Given
        createNotification("test1@example.com", "Test 1", "Message 1");
        createNotification("test2@example.com", "Test 2", "Message 2");

        // When - ищем до времени в будущем
        List<Notification> result = notificationRepository.findByStatusAndCreatedAtBefore("PENDING", LocalDateTime.now().plusHours(1));

        // Then - должно найти все уведомления
        assertEquals(2, result.size(), "Should find all notifications when searching in the future");
    }

    @Test
    void findByStatusAndCreatedAtBefore_ShouldReturnEmptyForWrongStatus() {
        // Given
        createNotification("test@example.com", "Test Subject", "Test message");

        // When - ищем с неправильным статусом
        List<Notification> result = notificationRepository.findByStatusAndCreatedAtBefore("SENT", LocalDateTime.now().plusHours(1));

        // Then - не должно найти
        assertEquals(0, result.size(), "Should not find notifications with wrong status");
    }

    @Test
    void findByStatusAndCreatedAtBefore_ShouldReturnEmptyForPastTime() {
        // Given
        createNotification("test@example.com", "Test Subject", "Test message");

        // When - ищем до времени в прошлом
        List<Notification> result = notificationRepository.findByStatusAndCreatedAtBefore("PENDING", LocalDateTime.now().minusHours(1));

        // Then - не должно найти (все уведомления созданы сейчас)
        assertEquals(0, result.size(), "Should not find notifications when searching in the past");
    }

    @Test
    void findByStatus_ShouldReturnCorrectNotifications() {
        // Given
        createNotification("test1@example.com", "Test 1", "Message 1");

        Notification pending2 = createNotification("user123", "Push Title", "Push message");
        pending2.setType("PUSH");
        notificationRepository.save(pending2);

        Notification sent = createNotification("test2@example.com", "Test 2", "Message 2");
        sent.setStatus("SENT");
        notificationRepository.save(sent);

        // When
        List<Notification> pendingResults = notificationRepository.findByStatus("PENDING");
        List<Notification> sentResults = notificationRepository.findByStatus("SENT");

        // Then
        assertEquals(2, pendingResults.size(), "Should find 2 PENDING notifications");
        assertEquals(1, sentResults.size(), "Should find 1 SENT notification");
        assertEquals("SENT", sentResults.get(0).getStatus());
    }

    @Test
    void saveNotification_ShouldGenerateIdAutomatically() {
        // Given
        Notification notification = createNotification("test@example.com", "Test Subject", "Test Message");

        // When
        Notification saved = notificationRepository.save(notification);

        // Then
        assertNotNull(saved.getId(), "ID should be generated automatically");
        assertFalse(saved.getId().isEmpty());
        assertEquals("EMAIL", saved.getType());
        assertEquals("PENDING", saved.getStatus());
        assertEquals("test@example.com", saved.getRecipient());
        assertNotNull(saved.getCreatedAt(), "CreatedAt should be set automatically");
    }

    @Test
    void findByRecipient_ShouldReturnCorrectNotifications() {
        // Given
        String recipient = "test@example.com";
        createNotification(recipient, "Subject 1", "Message 1");
        createNotification(recipient, "Subject 2", "Message 2");
        createNotification("other@example.com", "Other Subject", "Other message");

        // When
        List<Notification> result = notificationRepository.findByRecipient(recipient);

        // Then
        assertEquals(2, result.size(), "Should find 2 notifications for the recipient");
        assertTrue(result.stream().allMatch(n -> recipient.equals(n.getRecipient())));
    }

    @Test
    void findByType_ShouldReturnCorrectNotifications() {
        // Given
        createNotification("test1@example.com", "Email 1", "Message 1");
        createNotification("test2@example.com", "Email 2", "Message 2");

        Notification pushNotification = createNotification("user123", "Push", "Push message");
        pushNotification.setType("PUSH");
        notificationRepository.save(pushNotification);

        // When
        List<Notification> emailResults = notificationRepository.findByType("EMAIL");
        List<Notification> pushResults = notificationRepository.findByType("PUSH");

        // Then
        assertEquals(2, emailResults.size(), "Should find 2 EMAIL notifications");
        assertEquals(1, pushResults.size(), "Should find 1 PUSH notification");
        assertEquals("PUSH", pushResults.get(0).getType());
    }

    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        createNotification("test1@example.com", "Test 1", "Message 1");
        createNotification("test2@example.com", "Test 2", "Message 2");

        Notification sent = createNotification("test3@example.com", "Test 3", "Message 3");
        sent.setStatus("SENT");
        notificationRepository.save(sent);

        // When
        long pendingCount = notificationRepository.countByStatus("PENDING");
        long sentCount = notificationRepository.countByStatus("SENT");

        // Then
        assertEquals(2, pendingCount, "Should count 2 PENDING notifications");
        assertEquals(1, sentCount, "Should count 1 SENT notification");
    }

    @Test
    void findByStatusAndRetryCountLessThan_ShouldReturnCorrectNotifications() {
        // Given
        Notification lowRetry = createNotification("low@example.com", "Low Retry", "Message");
        lowRetry.setRetryCount(2);
        notificationRepository.save(lowRetry);

        Notification highRetry = createNotification("high@example.com", "High Retry", "Message");
        highRetry.setRetryCount(5);
        notificationRepository.save(highRetry);

        // When
        List<Notification> result = notificationRepository.findByStatusAndRetryCountLessThan("PENDING", 3);

        // Then
        assertEquals(1, result.size(), "Should find only notification with retry count less than 3");
        assertEquals("low@example.com", result.get(0).getRecipient());
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnAllForWideRange() {
        // Given
        createNotification("test1@example.com", "Test 1", "Message 1");
        createNotification("test2@example.com", "Test 2", "Message 2");

        // When - ищем в очень широком диапазоне
        LocalDateTime startTime = LocalDateTime.now().minusDays(1);
        LocalDateTime endTime = LocalDateTime.now().plusDays(1);
        List<Notification> result = notificationRepository.findByCreatedAtBetween(startTime, endTime);

        // Then - должно найти все уведомления
        assertEquals(2, result.size(), "Should find all notifications in wide time range");
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnEmptyForFutureRange() {
        // Given
        createNotification("test@example.com", "Test", "Message");

        // When - ищем в будущем диапазоне
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = LocalDateTime.now().plusHours(2);
        List<Notification> result = notificationRepository.findByCreatedAtBetween(startTime, endTime);

        // Then - не должно найти
        assertEquals(0, result.size(), "Should not find notifications in future time range");
    }

    @Test
    void findByCreatedAtBetween_ShouldReturnEmptyForPastRange() {
        // Given
        createNotification("test@example.com", "Test", "Message");

        // When - ищем в прошлом диапазоне
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now().minusHours(1);
        List<Notification> result = notificationRepository.findByCreatedAtBetween(startTime, endTime);

        // Then - не должно найти
        assertEquals(0, result.size(), "Should not find notifications in past time range");
    }

    // Тестируем сложные запросы аналитики
    @Test
    void findAverageProcessingTime_ShouldReturnNullWhenNoData() {
        // When
        Double result = notificationRepository.findAverageProcessingTime();

        // Then
        assertNull(result, "Should return null when no processing time data");
    }

    @Test
    void findAverageProcessingTime_ShouldCalculateAverage() {
        // Given
        Notification notification1 = createNotification("test1@example.com", "Test 1", "Message 1");
        notification1.setProcessingTime(100L);
        notificationRepository.save(notification1);

        Notification notification2 = createNotification("test2@example.com", "Test 2", "Message 2");
        notification2.setProcessingTime(200L);
        notificationRepository.save(notification2);

        // When
        Double result = notificationRepository.findAverageProcessingTime();

        // Then
        assertNotNull(result, "Should calculate average processing time");
        assertEquals(150.0, result, 0.01, "Average should be 150");
    }

    @Test
    void countByType_ShouldReturnCorrectCounts() {
        // Given
        createNotification("test1@example.com", "Email 1", "Message 1");
        createNotification("test2@example.com", "Email 2", "Message 2");

        Notification push = createNotification("user1", "Push 1", "Message");
        push.setType("PUSH");
        notificationRepository.save(push);

        // When
        List<Object[]> result = notificationRepository.countByType();

        // Then
        assertEquals(2, result.size(), "Should return counts for 2 types");

        // Проверяем что есть оба типа
        boolean hasEmail = result.stream().anyMatch(arr -> "EMAIL".equals(arr[0]) && 2L == (Long) arr[1]);
        boolean hasPush = result.stream().anyMatch(arr -> "PUSH".equals(arr[0]) && 1L == (Long) arr[1]);

        assertTrue(hasEmail, "Should have EMAIL type with count 2");
        assertTrue(hasPush, "Should have PUSH type with count 1");
    }

    // Вспомогательный метод для создания уведомления
    private Notification createNotification(String recipient, String subject, String message) {
        Notification notification = new Notification();
        notification.setType("EMAIL");
        notification.setStatus("PENDING");
        notification.setRecipient(recipient);
        notification.setSubject(subject);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }
}
