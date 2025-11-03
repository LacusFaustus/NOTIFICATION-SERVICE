package com.notificationservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest {

    public enum Priority {
        LOW, NORMAL, HIGH
    }

    @NotBlank(message = "Recipient email is required")
    @Email(message = "Invalid email format")
    private String to;

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message content is required")
    private String message;

    private String templateId;

    private Map<String, Object> templateVariables;

    @Email(message = "Invalid CC email format")
    private String cc;

    @Email(message = "Invalid BCC email format")
    private String bcc;

    private Map<String, String> attachments;

    @NotNull(message = "Priority is required")
    private Priority priority = Priority.NORMAL;

    // Validation method
    public boolean isValid() {
        return to != null && !to.trim().isEmpty() &&
                subject != null && !subject.trim().isEmpty() &&
                message != null && !message.trim().isEmpty();
    }
}
