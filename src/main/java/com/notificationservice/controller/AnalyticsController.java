package com.notificationservice.controller;

import com.notificationservice.dto.NotificationStats;
import com.notificationservice.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "API for notification analytics and statistics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics for a date range")
    public ResponseEntity<NotificationStats> getStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        NotificationStats stats = analyticsService.getNotificationStats(startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/daily-count")
    @Operation(summary = "Get daily notification counts")
    public ResponseEntity<Map<LocalDate, Long>> getDailyCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Map<LocalDate, Long> dailyCounts = analyticsService.getDailyNotificationCount(startDate, endDate);
        return ResponseEntity.ok(dailyCounts);
    }

    @GetMapping("/top-recipients")
    @Operation(summary = "Get top notification recipients")
    public ResponseEntity<Map<String, Long>> getTopRecipients(
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Long> topRecipients = analyticsService.getTopRecipients(limit);
        return ResponseEntity.ok(topRecipients);
    }

    @GetMapping("/failure-reasons")
    @Operation(summary = "Get failure reasons statistics")
    public ResponseEntity<Map<String, Long>> getFailureReasons() {
        Map<String, Long> failureReasons = analyticsService.getFailureReasons();
        return ResponseEntity.ok(failureReasons);
    }

    @GetMapping("/avg-processing-time")
    @Operation(summary = "Get average processing time")
    public ResponseEntity<Double> getAverageProcessingTime() {
        double avgTime = analyticsService.getAverageProcessingTime();
        return ResponseEntity.ok(avgTime);
    }
}
