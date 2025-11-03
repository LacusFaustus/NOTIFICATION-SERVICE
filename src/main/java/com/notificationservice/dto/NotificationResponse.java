package com.notificationservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String id;
    private String type;
    private String status;
    private String recipient;
    private String subject;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private String errorMessage;
    private Boolean success;

    public static NotificationResponse success(String id, String type, String recipient, String subject, String message) {
        return NotificationResponse.builder()
                .id(id)
                .type(type)
                .status("SENT")
                .recipient(recipient)
                .subject(subject)
                .message(message)
                .success(true)
                .createdAt(LocalDateTime.now())
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static NotificationResponse pending(String id, String type, String recipient, String subject) {
        return NotificationResponse.builder()
                .id(id)
                .type(type)
                .status("PENDING")
                .recipient(recipient)
                .subject(subject)
                .success(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static NotificationResponse failed(String id, String type, String recipient, String subject, String errorMessage) {
        return NotificationResponse.builder()
                .id(id)
                .type(type)
                .status("FAILED")
                .recipient(recipient)
                .subject(subject)
                .errorMessage(errorMessage)
                .success(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // Убрать дублирующий метод error, использовать только failed
    public static NotificationResponse error(String id, String type, String recipient, String errorMessage) {
        return failed(id, type, recipient, null, errorMessage);
    }

    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }
}
