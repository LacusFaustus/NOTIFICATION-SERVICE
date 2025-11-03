package com.notificationservice.controller;

import com.notificationservice.entity.NotificationTemplate;
import com.notificationservice.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Templates", description = "API for managing notification templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(summary = "Get all templates")
    public ResponseEntity<List<NotificationTemplate>> getAllTemplates() {
        // Добавляем метод в TemplateService или используем существующий
        List<NotificationTemplate> templates = templateService.findAllTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID")
    public ResponseEntity<NotificationTemplate> getTemplate(@PathVariable String id) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @PostMapping
    @Operation(summary = "Create new template")
    public ResponseEntity<NotificationTemplate> createTemplate(@Valid @RequestBody NotificationTemplate template) {
        return ResponseEntity.ok(templateService.createTemplate(template));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template")
    public ResponseEntity<NotificationTemplate> updateTemplate(
            @PathVariable String id,
            @Valid @RequestBody NotificationTemplate template) {
        return ResponseEntity.ok(templateService.updateTemplate(id, template));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String id) {
        templateService.deleteTemplate(id); // Добавляем метод в TemplateService
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cache/evict")
    @Operation(summary = "Evict template cache")
    public ResponseEntity<Void> evictTemplateCache(@PathVariable String id) {
        templateService.evictTemplateCache(id); // Исправлено название метода
        return ResponseEntity.ok().build();
    }
}
