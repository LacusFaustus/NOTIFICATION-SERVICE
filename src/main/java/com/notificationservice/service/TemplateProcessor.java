package com.notificationservice.service;

import com.notificationservice.entity.NotificationTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class TemplateProcessor {

    public String processTemplate(NotificationTemplate template, Map<String, Object> variables) {
        try {
            String processedContent = template.getContent();
            if (variables != null) {
                processedContent = replaceVariables(processedContent, variables);
            }

            return processedContent;

        } catch (Exception e) {
            log.error("Failed to process template {}: {}", template.getId(), e.getMessage());
            throw new RuntimeException("Template processing failed: " + e.getMessage(), e);
        }
    }

    public String processTemplate(String content, Map<String, Object> variables) {
        if (content == null) {
            return null;
        }

        if (variables != null) {
            return replaceVariables(content, variables);
        }

        return content;
    }

    private String replaceVariables(String content, Map<String, Object> variables) {
        String result = content;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        return result;
    }
}
