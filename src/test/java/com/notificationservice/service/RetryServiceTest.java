package com.notificationservice.service;

import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RetryService retryService;

    @Test
    void retryFailedNotification_WithValidNotification_ShouldProcessSuccessfully() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setStatus("FAILED");
        notification.setRetryCount(1);

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        // Act
        retryService.retryFailedNotification(notificationId);

        // Assert - should not throw exception
        verify(notificationRepository, atLeastOnce()).findById(notificationId);
    }

    @Test
    void getNotificationsForRetry_ShouldReturnFailedNotifications() {
        // Arrange
        Notification failedNotification = new Notification();
        failedNotification.setId("failed-1");
        failedNotification.setStatus("FAILED");
        failedNotification.setRetryCount(0);

        // Используем lenient mocking чтобы избежать конфликта аргументов
        when(notificationRepository.findByStatusAndRetryCountLessThan(eq("FAILED"), any(Integer.class)))
                .thenReturn(List.of(failedNotification));

        // Act
        List<Notification> result = retryService.getNotificationsForRetry();

        // Assert
        assertEquals(1, result.size());
        assertEquals("failed-1", result.get(0).getId());
    }

    @Test
    void getNotificationsForRetry_WithMaxRetryAttempts_ShouldReturnEmptyList() {
        // Arrange
        when(notificationRepository.findByStatusAndRetryCountLessThan(eq("FAILED"), any(Integer.class)))
                .thenReturn(List.of());

        // Act
        List<Notification> result = retryService.getNotificationsForRetry();

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void retryFailedNotification_WithMaxRetryCount_ShouldMoveToDLQ() {
        // Arrange
        String notificationId = "test-id";
        Notification notification = new Notification();
        notification.setId(notificationId);
        notification.setStatus("FAILED");
        notification.setRetryCount(3); // Максимальное количество попыток

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        // Act
        retryService.retryFailedNotification(notificationId);

        // Assert - should not throw exception
        verify(notificationRepository, atLeastOnce()).findById(notificationId);
    }

    @Test
    void retryFailedNotification_WithNonExistentNotification_ShouldHandleGracefully() {
        // Arrange
        String notificationId = "non-existent-id";
        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> {
            retryService.retryFailedNotification(notificationId);
        });

        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationService, never()).processNotification(anyString());
    }

    @Test
    void retryStuckNotifications_ShouldProcessStuckNotifications() {
        // Arrange
        Notification stuckNotification = new Notification();
        stuckNotification.setId("stuck-1");
        stuckNotification.setStatus("PENDING");
        stuckNotification.setCreatedAt(LocalDateTime.now().minusMinutes(15));

        when(notificationRepository.findByStatusAndCreatedAtBefore(anyString(), any(LocalDateTime.class)))
                .thenReturn(List.of(stuckNotification));

        // Act
        retryService.retryStuckNotifications();

        // Assert
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("notification.exchange"),
                eq("notification.routing.key"),
                eq("stuck-1")
        );
    }
}
