package com.notificationservice.controller;

import com.notificationservice.entity.Notification;
import com.notificationservice.service.RetryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/retry")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Retry", description = "API for manual retry operations")
public class RetryController {

    private final RetryService retryService;

    @PostMapping("/{notificationId}")
    @Operation(summary = "Retry a failed notification")
    public ResponseEntity<Void> retryNotification(@PathVariable String notificationId) {
        log.info("Manual retry requested for notification: {}", notificationId);

        retryService.retryFailedNotification(notificationId);

        return ResponseEntity.accepted().build();
    }

    @PostMapping("/failed")
    @Operation(summary = "Retry all failed notifications")
    public ResponseEntity<Void> retryAllFailed() {
        log.info("Manual retry requested for all failed notifications");

        List<Notification> failedNotifications = retryService.getNotificationsForRetry();

        for (Notification notification : failedNotifications) {
            retryService.retryFailedNotification(notification.getId());
        }

        return ResponseEntity.accepted().build();
    }

    @GetMapping("/failed")
    @Operation(summary = "Get all failed notifications eligible for retry")
    public ResponseEntity<List<Notification>> getFailedNotifications() {
        List<Notification> failedNotifications = retryService.getNotificationsForRetry();
        return ResponseEntity.ok(failedNotifications);
    }
}
