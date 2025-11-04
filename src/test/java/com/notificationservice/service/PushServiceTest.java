package com.notificationservice.service;

import com.notificationservice.entity.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PushServiceTest {

    @Mock
    private MetricsService metricsService;

    @InjectMocks
    private PushService pushService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setId("test-id");
        testNotification.setRecipient("user-123");
        testNotification.setTitle("Test Title");
        testNotification.setMessage("Test Message");
    }

    @Test
    void sendPush_WithValidNotification_ShouldSendSuccessfully() {
        // Act
        pushService.sendPush(testNotification);

        // Assert - should complete without exceptions
        verify(metricsService, never()).recordPushFailed();
    }

    @Test
    void sendPush_WithNullNotification_ShouldThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pushService.sendPush(null);
        });

        assertEquals("Notification cannot be null", exception.getMessage());
    }
}
