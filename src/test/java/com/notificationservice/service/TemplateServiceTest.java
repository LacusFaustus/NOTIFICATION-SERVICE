package com.notificationservice.service;

import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.repository.TemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TemplateServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private TemplateProcessor templateProcessor;

    @InjectMocks
    private TemplateService templateService;

    @Test
    void getTemplate_WithExistingId_ShouldReturnTemplate() {
        // Arrange
        String templateId = "test-template";
        NotificationTemplate template = new NotificationTemplate();
        template.setId(templateId);
        template.setName("Test Template");

        when(templateRepository.findById(templateId)).thenReturn(Optional.of(template));

        // Act
        NotificationTemplate result = templateService.getTemplate(templateId);

        // Assert
        assertNotNull(result);
        assertEquals(templateId, result.getId());
        verify(templateRepository, times(1)).findById(templateId);
    }

    @Test
    void findAllTemplates_ShouldReturnAllTemplates() {
        // Arrange
        NotificationTemplate template1 = new NotificationTemplate();
        template1.setId("template-1");
        NotificationTemplate template2 = new NotificationTemplate();
        template2.setId("template-2");

        when(templateRepository.findAll()).thenReturn(List.of(template1, template2));

        // Act
        List<NotificationTemplate> result = templateService.findAllTemplates();

        // Assert
        assertEquals(2, result.size());
        verify(templateRepository, times(1)).findAll();
    }

    @Test
    void createTemplate_WithValidTemplate_ShouldSaveAndReturn() {
        // Arrange
        NotificationTemplate template = new NotificationTemplate();
        template.setName("New Template");
        template.setType("EMAIL");
        template.setSubject("Test Subject");
        template.setContent("Test Content");

        when(templateRepository.findByName("New Template")).thenReturn(Optional.empty());
        when(templateRepository.save(any(NotificationTemplate.class))).thenReturn(template);

        // Act
        NotificationTemplate result = templateService.createTemplate(template);

        // Assert
        assertNotNull(result);
        verify(templateRepository, times(1)).save(any(NotificationTemplate.class));
    }
}
