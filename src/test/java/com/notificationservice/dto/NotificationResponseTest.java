package com.notificationservice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationResponseTest {

    @Test
    void success_ShouldCreateCorrectResponse() {
        // When
        NotificationResponse response = NotificationResponse.success(
                "test-id", "EMAIL", "test@example.com", "Test Subject", "Test Message"
        );

        // Then
        assertEquals("test-id", response.getId());
        assertEquals("EMAIL", response.getType());
        assertEquals("SENT", response.getStatus());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals("Test Subject", response.getSubject());
        assertEquals("Test Message", response.getMessage());
        assertTrue(response.isSuccess());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getSentAt());
    }

    @Test
    void pending_ShouldCreateCorrectResponse() {
        // When
        NotificationResponse response = NotificationResponse.pending(
                "test-id", "PUSH", "user-123", "Test Title"
        );

        // Then
        assertEquals("test-id", response.getId());
        assertEquals("PUSH", response.getType());
        assertEquals("PENDING", response.getStatus());
        assertEquals("user-123", response.getRecipient());
        assertEquals("Test Title", response.getSubject());
        assertTrue(response.isSuccess());
        assertNotNull(response.getCreatedAt());
        assertNull(response.getSentAt());
    }

    @Test
    void failed_ShouldCreateCorrectResponse() {
        // When
        NotificationResponse response = NotificationResponse.failed(
                "test-id", "EMAIL", "test@example.com", "Test Subject", "SMTP error"
        );

        // Then
        assertEquals("test-id", response.getId());
        assertEquals("EMAIL", response.getType());
        assertEquals("FAILED", response.getStatus());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals("Test Subject", response.getSubject());
        assertEquals("SMTP error", response.getErrorMessage());
        assertFalse(response.isSuccess());
        assertNotNull(response.getCreatedAt());
        assertNull(response.getSentAt());
    }

    @Test
    void error_ShouldCreateCorrectResponse() {
        // When
        NotificationResponse response = NotificationResponse.error(
                "test-id", "PUSH", "user-123", "Push service unavailable"
        );

        // Then
        assertEquals("test-id", response.getId());
        assertEquals("PUSH", response.getType());
        assertEquals("FAILED", response.getStatus());
        assertEquals("user-123", response.getRecipient());
        assertEquals("Push service unavailable", response.getErrorMessage());
        assertFalse(response.isSuccess());
    }

    @Test
    void builder_ShouldCreateCorrectResponse() {
        // When
        NotificationResponse response = NotificationResponse.builder()
                .id("test-id")
                .type("EMAIL")
                .status("SENT")
                .recipient("test@example.com")
                .subject("Test Subject")
                .message("Test Message")
                .success(true)
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();

        // Then
        assertEquals("test-id", response.getId());
        assertEquals("EMAIL", response.getType());
        assertEquals("SENT", response.getStatus());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals("Test Subject", response.getSubject());
        assertEquals("Test Message", response.getMessage());
        assertTrue(response.isSuccess());
        assertNotNull(response.getCreatedAt());
        assertNotNull(response.getSentAt());
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        // When
        NotificationResponse response = new NotificationResponse();

        // Then
        assertNull(response.getId());
        assertNull(response.getType());
        assertNull(response.getStatus());
        assertNull(response.getRecipient());
        assertNull(response.getSubject());
        assertNull(response.getMessage());
        assertNull(response.getSuccess());
        assertNull(response.getCreatedAt());
        assertNull(response.getSentAt());
        assertNull(response.getErrorMessage());
    }

    @Test
    void allArgsConstructor_ShouldWork() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // When
        NotificationResponse response = new NotificationResponse(
                "test-id", "EMAIL", "SENT", "test@example.com",
                "Test Subject", "Test Message", now, now, "No error", true
        );

        // Then
        assertEquals("test-id", response.getId());
        assertEquals("EMAIL", response.getType());
        assertEquals("SENT", response.getStatus());
        assertEquals("test@example.com", response.getRecipient());
        assertEquals("Test Subject", response.getSubject());
        assertEquals("Test Message", response.getMessage());
        assertEquals(now, response.getCreatedAt());
        assertEquals(now, response.getSentAt());
        assertEquals("No error", response.getErrorMessage());
        assertTrue(response.isSuccess());
    }
}
