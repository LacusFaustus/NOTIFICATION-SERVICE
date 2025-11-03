package com.notificationservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(nullable = false, length = 50)
    private String status = "PENDING";

    @Column(nullable = false, length = 255)
    private String recipient;

    @Column(length = 500)
    private String subject;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "template_id", length = 36)
    private String templateId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count")
    private int retryCount = 0;

    @Column(length = 20)
    private String priority = "NORMAL";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "processing_time")
    private Long processingTime;

    // Добавляем поле title для push уведомлений
    @Column(length = 500)
    private String title;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "PENDING";
        }
        if (priority == null) {
            priority = "NORMAL";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
