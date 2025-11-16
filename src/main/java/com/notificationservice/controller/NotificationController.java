package com.notificationservice.controller;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "API for sending notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/email")
    @Operation(summary = "Send email notification")
    public ResponseEntity<NotificationResponse> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        log.info("Received email request for: {}", emailRequest.getTo());
        NotificationResponse response = notificationService.sendEmail(emailRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification status")
    public ResponseEntity<Notification> getNotificationStatus(@PathVariable String id) {
        Notification notification = notificationService.getNotificationStatus(id);
        return ResponseEntity.ok(notification);
    }
}
