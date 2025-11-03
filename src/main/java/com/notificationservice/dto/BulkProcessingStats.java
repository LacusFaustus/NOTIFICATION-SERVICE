package com.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkProcessingStats {
    private int totalProcessed = 0;
    private int successCount = 0;
    private int failureCount = 0;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long processingTimeMs = 0;

    public double getSuccessRate() {
        return totalProcessed > 0 ? (successCount * 100.0) / totalProcessed : 0.0;
    }

    public void complete() {
        this.endTime = LocalDateTime.now();
        if (this.startTime != null) {
            this.processingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }
}
