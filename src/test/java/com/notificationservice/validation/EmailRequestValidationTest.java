package com.notificationservice.validation;

import com.notificationservice.dto.EmailRequest;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmailRequestValidationTest {

    private final Validator validator;

    public EmailRequestValidationTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenValidEmailRequest_ThenNoViolations() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("Test message content");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertTrue(violations.isEmpty(), "Should have no violations for valid email request");
    }

    @Test
    void whenInvalidEmail_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("invalid-email");
        request.setSubject("Test Subject");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for invalid email format");
        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().toLowerCase().contains("email"));
    }

    @Test
    void whenEmptyEmail_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("");
        request.setSubject("Test Subject");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for empty email");
    }

    @Test
    void whenNullEmail_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo(null);
        request.setSubject("Test Subject");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for null email");
    }

    @Test
    void whenEmptySubject_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("");
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for empty subject");
    }

    @Test
    void whenNullSubject_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject(null);
        request.setMessage("Test message");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for null subject");
    }

    @Test
    void whenEmptyMessage_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage("");

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for empty message");
    }

    @Test
    void whenNullMessage_ThenViolationOccurs() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("test@example.com");
        request.setSubject("Test Subject");
        request.setMessage(null);

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have violations for null message");
    }

    @Test
    void whenAllFieldsInvalid_ThenMultipleViolations() {
        // Given
        EmailRequest request = new EmailRequest();
        request.setTo("invalid");
        request.setSubject("");
        request.setMessage(null);

        // When
        Set<ConstraintViolation<EmailRequest>> violations = validator.validate(request);

        // Then
        assertFalse(violations.isEmpty(), "Should have multiple violations");
        assertTrue(violations.size() >= 3, "Should have at least 3 violations");
    }
}
