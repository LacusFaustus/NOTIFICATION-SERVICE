package com.notificationservice.service;

import com.notificationservice.entity.NotificationTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TemplateProcessorTest {

    private TemplateProcessor templateProcessor;

    @BeforeEach
    void setUp() {
        templateProcessor = new TemplateProcessor();
    }

    @Test
    void processTemplate_WithValidTemplateAndVariables_ShouldReplacePlaceholders() {
        // Arrange
        NotificationTemplate template = new NotificationTemplate();
        template.setContent("Hello {{name}}, welcome to {{company}}!");

        Map<String, Object> variables = Map.of(
                "name", "John",
                "company", "TestCorp"
        );

        // Act
        String result = templateProcessor.processTemplate(template, variables);

        // Assert
        assertEquals("Hello John, welcome to TestCorp!", result);
    }

    @Test
    void processTemplate_WithNullVariables_ShouldReturnOriginalContent() {
        // Arrange
        NotificationTemplate template = new NotificationTemplate();
        template.setContent("Hello {{name}}!");

        // Act
        String result = templateProcessor.processTemplate(template, null);

        // Assert
        assertEquals("Hello {{name}}!", result);
    }

    @Test
    void processTemplate_WithStringContentAndVariables_ShouldReplacePlaceholders() {
        // Arrange
        String content = "Your order {{orderId}} is ready. Total: {{amount}}";
        Map<String, Object> variables = Map.of(
                "orderId", "12345",
                "amount", "$99.99"
        );

        // Act
        String result = templateProcessor.processTemplate(content, variables);

        // Assert
        assertEquals("Your order 12345 is ready. Total: $99.99", result);
    }

    @Test
    void processTemplate_WithMissingVariables_ShouldKeepPlaceholders() {
        // Arrange
        String content = "Hello {{name}}, your code is {{code}}";
        Map<String, Object> variables = Map.of("name", "John");

        // Act
        String result = templateProcessor.processTemplate(content, variables);

        // Assert
        assertEquals("Hello John, your code is {{code}}", result);
    }
}
