package com.notificationservice.controller;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.dto.PushRequest;
import com.notificationservice.entity.Notification;
import com.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "API for sending notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        log.info("Received email request for: {}", emailRequest.getTo());
        NotificationResponse response = notificationService.sendEmail(emailRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/push")
    public ResponseEntity<NotificationResponse> sendPush(@Valid @RequestBody PushRequest pushRequest) {
        log.info("Received push request for user: {}", pushRequest.getUserId());
        NotificationResponse response = notificationService.sendPush(pushRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationStatus(@PathVariable String id) {
        Notification notification = notificationService.getNotificationStatus(id);
        return ResponseEntity.ok(notification);
    }

    // Fallback methods
    public ResponseEntity<NotificationResponse> sendEmailFallback(EmailRequest request, Exception e) {
        log.error("Email service fallback triggered for: {}", request.getTo(), e);
        NotificationResponse response = NotificationResponse.error(
                null, "EMAIL", request.getTo(), "Service temporarily unavailable. Please try again later."
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<NotificationResponse> sendPushFallback(PushRequest request, Exception e) {
        log.error("Push service fallback triggered for user: {}", request.getUserId(), e);
        NotificationResponse response = NotificationResponse.error(
                null, "PUSH", request.getUserId(), "Service temporarily unavailable. Please try again later."
        );
        return ResponseEntity.ok(response);
    }
}
