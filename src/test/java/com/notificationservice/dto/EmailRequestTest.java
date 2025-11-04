package com.notificationservice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailRequestTest {

    @Test
    void emailRequest_ShouldHaveCorrectDefaults() {
        // Arrange & Act
        EmailRequest request = new EmailRequest();

        // Assert
        assertNull(request.getTo());
        assertNull(request.getSubject());
        assertNull(request.getMessage());
        assertEquals(EmailRequest.Priority.NORMAL, request.getPriority());
    }

    @Test
    void emailRequest_WithAllValues_ShouldSetCorrectly() {
        // Arrange & Act
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");
        request.setPriority(EmailRequest.Priority.HIGH);
        request.setTemplateId("welcome-template");
        request.setCc("cc@example.com");
        request.setBcc("bcc@example.com");

        // Assert
        assertEquals("test@example.com", request.getTo());
        assertEquals("Test Subject", request.getSubject());
        assertEquals("Test Message", request.getMessage());
        assertEquals(EmailRequest.Priority.HIGH, request.getPriority());
        assertEquals("welcome-template", request.getTemplateId());
        assertEquals("cc@example.com", request.getCc());
        assertEquals("bcc@example.com", request.getBcc());
    }

    @Test
    void isValid_WithValidRequest_ShouldReturnTrue() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test Message");

        // Act & Assert
        assertTrue(request.isValid());
    }

    @Test
    void isValid_WithInvalidRequest_ShouldReturnFalse() {
        // Arrange
        EmailRequest request = new EmailRequest();
        request.setTo("");
        request.setSubject("");
        request.setMessage("");

        // Act & Assert
        assertFalse(request.isValid());
    }
}
