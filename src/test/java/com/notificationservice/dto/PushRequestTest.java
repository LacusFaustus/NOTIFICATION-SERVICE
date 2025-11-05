package com.notificationservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PushRequestTest {

    @Test
    void isValid_WithValidRequest_ShouldReturnTrue() {
        // Arrange
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle("Test Title");
        request.setMessage("Test Message");

        // Act & Assert
        assertTrue(request.isValid());
    }

    @Test
    void isValid_WithInvalidRequest_ShouldReturnFalse() {
        // Arrange
        PushRequest request = new PushRequest();
        request.setUserId("");
        request.setTitle("");
        request.setMessage("");

        // Act & Assert
        assertFalse(request.isValid());
    }

    @Test
    void isValid_WithNullValues_ShouldReturnFalse() {
        // Arrange
        PushRequest request = new PushRequest();
        request.setUserId(null);
        request.setTitle(null);
        request.setMessage(null);

        // Act & Assert
        assertFalse(request.isValid());
    }

    @Test
    void isValid_WithPartialNullValues_ShouldReturnFalse() {
        // Arrange
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle(null);
        request.setMessage("Test Message");

        // Act & Assert
        assertFalse(request.isValid());
    }
}
