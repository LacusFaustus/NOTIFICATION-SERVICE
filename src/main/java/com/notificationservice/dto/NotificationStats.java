package com.notificationservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStats {
    private int totalNotifications;
    private double successRate;
    private Map<String, Long> notificationsByType;
    private Map<String, Long> notificationsByStatus;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private long averageProcessingTimeMs;

    public long getSuccessfulCount() {
        return notificationsByStatus.getOrDefault("SENT", 0L);
    }

    public long getFailedCount() {
        return notificationsByStatus.getOrDefault("FAILED", 0L);
    }

    public long getPendingCount() {
        return notificationsByStatus.getOrDefault("PENDING", 0L);
    }
}
