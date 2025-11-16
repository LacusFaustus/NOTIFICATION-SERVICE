package com.notificationservice.service;

import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.repository.TemplateRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateProcessor templateProcessor;

    public String processTemplate(String templateId, Map<String, Object> variables) {
        if (templateId == null || templateId.trim().isEmpty()) {
            throw new IllegalArgumentException("Template ID cannot be null or empty");
        }

        try {
            NotificationTemplate template = getTemplate(templateId);
            if (template == null) {
                throw new RuntimeException("Template not found: " + templateId);
            }

            String processedContent = templateProcessor.processTemplate(template, variables);
            log.info("Template processed successfully: {}", templateId);
            return processedContent;

        } catch (Exception e) {
            log.error("Failed to process template: {}", templateId, e);
            throw new RuntimeException("Template processing failed: " + e.getMessage(), e);
        }
    }

    @Cacheable(value = "templates", key = "#id")
    public NotificationTemplate getTemplate(String id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Template not found with id: " + id));
    }

    public List<NotificationTemplate> findAllTemplates() {
        return templateRepository.findAll();
    }

    public List<NotificationTemplate> findActiveTemplates() {
        return templateRepository.findByActiveTrue();
    }

    public List<NotificationTemplate> findTemplatesByType(String type) {
        return templateRepository.findByType(type);
    }

    public NotificationTemplate createTemplate(@Valid NotificationTemplate template) {
        // Check if template with same name already exists
        Optional<NotificationTemplate> existingTemplate = templateRepository.findByName(template.getName());
        if (existingTemplate.isPresent()) {
            throw new RuntimeException("Template with name '" + template.getName() + "' already exists");
        }

        template.setId(UUID.randomUUID().toString());
        NotificationTemplate savedTemplate = templateRepository.save(template);
        log.info("Template created successfully: {}", savedTemplate.getId());
        return savedTemplate;
    }

    public NotificationTemplate updateTemplate(String id, @Valid NotificationTemplate template) {
        NotificationTemplate existingTemplate = getTemplate(id);

        existingTemplate.setName(template.getName());
        existingTemplate.setType(template.getType());
        existingTemplate.setSubject(template.getSubject());
        existingTemplate.setContent(template.getContent());
        existingTemplate.setVariables(template.getVariables());
        existingTemplate.setVersion(template.getVersion());
        existingTemplate.setActive(template.isActive());

        NotificationTemplate updatedTemplate = templateRepository.save(existingTemplate);
        evictTemplateCache(id);
        log.info("Template updated successfully: {}", id);
        return updatedTemplate;
    }

    public void deleteTemplate(String id) {
        NotificationTemplate template = getTemplate(id);
        templateRepository.delete(template);
        evictTemplateCache(id);
        log.info("Template deleted successfully: {}", id);
    }

    @CacheEvict(value = "templates", key = "#id")
    public void evictTemplateCache(String id) {
        log.debug("Template cache evicted for: {}", id);
    }

    public NotificationTemplate findByName(String name) {
        return templateRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Template not found with name: " + name));
    }

    // Helper method to generate UUID
    private String generateId() {
        return java.util.UUID.randomUUID().toString();
    }
}
