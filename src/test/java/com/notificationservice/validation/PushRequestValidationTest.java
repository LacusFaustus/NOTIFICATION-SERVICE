package com.notificationservice.validation;

import com.notificationservice.dto.PushRequest;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PushRequestValidationTest {

    private final Validator validator;

    public PushRequestValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidPushRequest_ThenNoViolations() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle("Test Title");
        request.setMessage("Test message content");
        request.setPlatform("IOS");

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty(), "Should have no violations for valid push request");
    }

    @Test
    void whenEmptyUserId_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("");
        request.setTitle("Test Title");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for empty user ID");
    }

    @Test
    void whenNullUserId_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId(null);
        request.setTitle("Test Title");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for null user ID");
    }

    @Test
    void whenEmptyTitle_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle("");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for empty title");
    }

    @Test
    void whenNullTitle_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle(null);
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for null title");
    }

    @Test
    void whenEmptyMessage_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle("Test Title");
        request.setMessage("");

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for empty message");
    }

    @Test
    void whenNullMessage_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle("Test Title");
        request.setMessage(null);

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for null message");
    }

    @Test
    void whenInvalidPlatform_ThenViolationOccurs() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("user-123");
        request.setTitle("Test Title");
        request.setMessage("Test message");
        request.setPlatform("INVALID_PLATFORM"); // Невалидная платформа

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for invalid platform");

        // Проверяем что есть violation именно для поля platform
        boolean hasPlatformViolation = violations.stream()
                .anyMatch(v -> "platform".equals(v.getPropertyPath().toString()));
        assertTrue(hasPlatformViolation, "Should have violation for platform field");
    }

    @Test
    void whenAllFieldsInvalid_ThenMultipleViolations() {
        // Given
        PushRequest request = new PushRequest();
        request.setUserId("");
        request.setTitle("");
        request.setMessage(null);

        // When
        Set<ConstraintViolation<PushRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have multiple violations");
        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }
}
