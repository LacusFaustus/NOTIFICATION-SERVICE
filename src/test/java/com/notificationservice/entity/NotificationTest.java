package com.notificationservice.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationTest {

    @Test
    void notification_ShouldHaveCorrectDefaults() {
        // Arrange & Act
        Notification notification = new Notification();

        // Assert
        assertNull(notification.getId());
        assertNull(notification.getType());
        assertEquals("PENDING", notification.getStatus());
        assertNull(notification.getRecipient());
        assertNull(notification.getSubject());
        assertNull(notification.getMessage());
        assertEquals(0, notification.getRetryCount());
        assertEquals("NORMAL", notification.getPriority());
        assertNull(notification.getCreatedAt());
        assertNull(notification.getUpdatedAt());
        assertNull(notification.getSentAt());
    }

    @Test
    void notification_WithAllValues_ShouldSetCorrectly() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();

        // Act
        Notification notification = new Notification();
        notification.setId("test-id");
        notification.setType("EMAIL");
        notification.setStatus("SENT");
        notification.setRecipient("test@example.com");
        notification.setSubject("Test Subject");
        notification.setMessage("Test Message");
        notification.setRetryCount(2);
        notification.setPriority("HIGH");
        notification.setCreatedAt(now);
        notification.setUpdatedAt(now);
        notification.setSentAt(now);
        notification.setTitle("Test Title");

        // Assert
        assertEquals("test-id", notification.getId());
        assertEquals("EMAIL", notification.getType());
        assertEquals("SENT", notification.getStatus());
        assertEquals("test@example.com", notification.getRecipient());
        assertEquals("Test Subject", notification.getSubject());
        assertEquals("Test Message", notification.getMessage());
        assertEquals(2, notification.getRetryCount());
        assertEquals("HIGH", notification.getPriority());
        assertEquals(now, notification.getCreatedAt());
        assertEquals(now, notification.getUpdatedAt());
        assertEquals(now, notification.getSentAt());
        assertEquals("Test Title", notification.getTitle());
    }
}
