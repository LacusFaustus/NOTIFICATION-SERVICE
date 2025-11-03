package com.notificationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @InjectMocks
    private TemplateService templateService;

    @Test
    void processTemplate_WithValidTemplateAndVariables_ShouldReturnProcessedContent() {
        // Arrange
        String templateId = "welcome-template";
        Map<String, Object> variables = Map.of("name", "John", "company", "Test Corp");

        // Act
        String result = templateService.processTemplate(templateId, variables);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("John"));
        assertTrue(result.contains("Test Corp"));
    }

    @Test
    void processTemplate_WithNullTemplateId_ShouldThrowException() {
        // Arrange
        Map<String, Object> variables = Map.of("name", "John");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.processTemplate(null, variables);
        });
    }

    @Test
    void processTemplate_WithEmptyTemplateId_ShouldThrowException() {
        // Arrange
        Map<String, Object> variables = Map.of("name", "John");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            templateService.processTemplate("", variables);
        });
    }

    @Test
    void processTemplate_WithNonExistentTemplate_ShouldReturnDefaultMessage() {
        // Arrange
        String templateId = "non-existent-template";
        Map<String, Object> variables = Map.of("name", "John");

        // Act
        String result = templateService.processTemplate(templateId, variables);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Template not found"));
    }
}
