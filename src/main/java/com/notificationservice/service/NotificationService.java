package com.notificationservice.service;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.entity.Notification;
import com.notificationservice.exception.NotificationNotFoundException;
import com.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final MetricsService metricsService;

    @Transactional
    public NotificationResponse sendEmail(EmailRequest request) {
        log.info("Processing email request for: {}", request.getTo());

        if (!isValidEmailRequest(request)) {
            log.warn("Invalid email request for: {}", request.getTo());
            metricsService.recordEmailFailed();
            return NotificationResponse.failed(
                    null, "EMAIL", request.getTo(), request.getSubject(),
                    "Invalid email request: missing required fields"
            );
        }

        try {
            Notification notification = createEmailNotification(request);
            Notification savedNotification = notificationRepository.save(notification);

            emailService.sendEmail(savedNotification);

            savedNotification.setStatus("SENT");
            savedNotification.setSentAt(LocalDateTime.now());
            notificationRepository.save(savedNotification);

            metricsService.recordEmailSent();
            log.info("Email sent successfully to: {}", request.getTo());

            return NotificationResponse.success(
                    savedNotification.getId(),
                    "EMAIL",
                    savedNotification.getRecipient(),
                    savedNotification.getSubject(),
                    savedNotification.getMessage()
            );

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", request.getTo(), e.getMessage());
            metricsService.recordEmailFailed();

            Notification failedNotification = createEmailNotification(request);
            failedNotification.setStatus("FAILED");
            failedNotification.setErrorMessage(e.getMessage());
            Notification savedFailed = notificationRepository.save(failedNotification);

            return NotificationResponse.failed(
                    savedFailed.getId(),
                    "EMAIL",
                    savedFailed.getRecipient(),
                    savedFailed.getSubject(),
                    e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public Notification getNotificationStatus(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
    }

    private boolean isValidEmailRequest(EmailRequest request) {
        return request.getTo() != null && !request.getTo().isEmpty() &&
                request.getSubject() != null && !request.getSubject().isEmpty() &&
                request.getMessage() != null && !request.getMessage().isEmpty();
    }

    private Notification createEmailNotification(EmailRequest request) {
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID().toString());
        notification.setType("EMAIL");
        notification.setStatus("PENDING");
        notification.setRecipient(request.getTo());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setPriority("NORMAL");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
