package com.notificationservice.controller;

import com.notificationservice.dto.BulkEmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.service.BulkNotificationService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/notifications/bulk")
@RequiredArgsConstructor
public class BulkNotificationController {

    private final BulkNotificationService bulkNotificationService;

    @PostMapping("/email")
    @RateLimiter(name = "bulkEmailRateLimit")
    public ResponseEntity<List<NotificationResponse>> sendBulkEmails(
            @Valid @RequestBody BulkEmailRequest request) {

        log.info("Received bulk email request for {} recipients", request.getEmails().size());

        List<NotificationResponse> responses = bulkNotificationService.sendBulkEmails(request);

        return ResponseEntity.ok(responses);
    }

    @PostMapping("/email/async")
    @RateLimiter(name = "bulkEmailRateLimit")
    public CompletableFuture<ResponseEntity<List<NotificationResponse>>> sendBulkEmailsAsync(
            @Valid @RequestBody BulkEmailRequest request) {

        log.info("Received async bulk email request for {} recipients", request.getEmails().size());

        return bulkNotificationService.sendBulkEmailsAsync(request)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Bulk notification service is healthy");
    }
}
