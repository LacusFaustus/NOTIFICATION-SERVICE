package com.notificationservice.service;

import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import com.notificationservice.dto.PushRequest;
import com.notificationservice.entity.Notification;
import com.notificationservice.exception.NotificationNotFoundException;
import com.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final PushService pushService;
    private final TemplateService templateService;
    private final MetricsService metricsService;

    @Transactional
    public NotificationResponse sendEmail(EmailRequest request) {
        String notificationId = generateId();

        try {
            // Validate request
            if (!isValidEmailRequest(request)) {
                log.warn("Invalid email request: {}", request);
                metricsService.recordEmailFailed();
                return NotificationResponse.failed(
                        notificationId, "EMAIL", request.getTo(),
                        request.getSubject(), "Invalid email request"
                );
            }

            // Process template if provided
            String finalMessage = request.getMessage();
            if (request.getTemplateId() != null && !request.getTemplateId().isEmpty()) {
                try {
                    String processedContent = templateService.processTemplate(
                            request.getTemplateId(),
                            request.getTemplateVariables() != null ? request.getTemplateVariables() : Map.of()
                    );
                    finalMessage = processedContent;
                } catch (Exception e) {
                    log.warn("Failed to process template {}, using fallback message", request.getTemplateId(), e);
                    // Continue with original message if template processing fails
                }
            }

            // Create and save notification entity with PENDING status
            Notification notification = createEmailNotification(request);
            notification.setMessage(finalMessage);

            Notification savedNotification = notificationRepository.save(notification);
            log.info("Saved email notification with ID: {}", savedNotification.getId());

            try {
                // Send email - этот вызов может выбросить исключение
                emailService.sendEmail(notification);

                // Если отправка успешна - обновляем статус на SENT
                savedNotification.setStatus("SENT");
                savedNotification.setSentAt(LocalDateTime.now());
                notificationRepository.save(savedNotification);

                metricsService.recordEmailSent();
                log.info("Email sent successfully for notification ID: {}", savedNotification.getId());

                return NotificationResponse.success(
                        savedNotification.getId(),
                        "EMAIL",
                        request.getTo(),
                        request.getSubject(),
                        "Email sent successfully"
                );

            } catch (Exception e) {
                log.error("Failed to send email for notification ID: {}", savedNotification.getId(), e);
                // Если отправка не удалась - обновляем статус на FAILED
                savedNotification.setStatus("FAILED");
                savedNotification.setErrorMessage(e.getMessage());
                notificationRepository.save(savedNotification);

                metricsService.recordEmailFailed();
                return NotificationResponse.failed(
                        savedNotification.getId(),
                        "EMAIL",
                        request.getTo(),
                        request.getSubject(),
                        e.getMessage()
                );
            }

        } catch (Exception e) {
            log.error("Unexpected error during email sending", e);
            metricsService.recordEmailFailed();
            return NotificationResponse.failed(
                    notificationId, "EMAIL", request.getTo(),
                    request.getSubject(), "Unexpected error: " + e.getMessage()
            );
        }
    }

    @Async
    public CompletableFuture<NotificationResponse> sendEmailAsync(EmailRequest request) {
        return CompletableFuture.completedFuture(sendEmail(request));
    }

    @Transactional
    public NotificationResponse sendPush(PushRequest request) {
        String notificationId = generateId();

        try {
            // Validate request
            if (!isValidPushRequest(request)) {
                log.warn("Invalid push request: {}", request);
                metricsService.recordPushFailed();
                return NotificationResponse.failed(
                        notificationId, "PUSH", request.getUserId(),
                        request.getTitle(), "Invalid push request"
                );
            }

            // Create notification entity
            Notification notification = createPushNotification(request);

            // Save initial notification
            Notification savedNotification = notificationRepository.save(notification);
            log.info("Saved push notification with ID: {}", savedNotification.getId());

            try {
                // Send push notification
                pushService.sendPush(notification);

                // Update notification status
                savedNotification.setStatus("SENT");
                savedNotification.setSentAt(LocalDateTime.now());
                notificationRepository.save(savedNotification);

                metricsService.recordPushSent();
                log.info("Push notification sent successfully for notification ID: {}", savedNotification.getId());

                return NotificationResponse.success(
                        savedNotification.getId(),
                        "PUSH",
                        request.getUserId(),
                        request.getTitle(),
                        "Push notification sent successfully"
                );

            } catch (Exception e) {
                log.error("Failed to send push notification for ID: {}", savedNotification.getId(), e);
                savedNotification.setStatus("FAILED");
                savedNotification.setErrorMessage(e.getMessage());
                notificationRepository.save(savedNotification);

                metricsService.recordPushFailed();
                return NotificationResponse.failed(
                        savedNotification.getId(),
                        "PUSH",
                        request.getUserId(),
                        request.getTitle(),
                        e.getMessage()
                );
            }

        } catch (Exception e) {
            log.error("Unexpected error during push notification sending", e);
            metricsService.recordPushFailed();
            return NotificationResponse.failed(
                    notificationId, "PUSH", request.getUserId(),
                    request.getTitle(), "Unexpected error: " + e.getMessage()
            );
        }
    }

    @Transactional(readOnly = true)
    public Notification getNotificationStatus(String id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with id: " + id));
    }

    @Transactional
    public void processNotification(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        if (!"PENDING".equals(notification.getStatus())) {
            log.info("Notification {} is not in PENDING status, skipping processing", notificationId);
            return;
        }

        try {
            switch (notification.getType()) {
                case "EMAIL":
                    emailService.sendEmail(notification);
                    break;
                case "PUSH":
                    pushService.sendPush(notification);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported notification type: " + notification.getType());
            }

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);
            log.info("Successfully processed notification: {}", notificationId);

        } catch (Exception e) {
            log.error("Failed to process notification: {}", notificationId, e);
            notification.setStatus("FAILED");
            notification.setErrorMessage(e.getMessage());
            notificationRepository.save(notification);
            throw e;
        }
    }

    private boolean isValidEmailRequest(EmailRequest request) {
        return request.getTo() != null && !request.getTo().isEmpty() &&
                request.getSubject() != null && !request.getSubject().isEmpty() &&
                request.getMessage() != null && !request.getMessage().isEmpty();
    }

    private boolean isValidPushRequest(PushRequest request) {
        return request.getUserId() != null && !request.getUserId().isEmpty() &&
                request.getTitle() != null && !request.getTitle().isEmpty() &&
                request.getMessage() != null && !request.getMessage().isEmpty();
    }

    private Notification createEmailNotification(EmailRequest request) {
        Notification notification = new Notification();
        notification.setId(generateId());
        notification.setType("EMAIL");
        notification.setStatus("PENDING"); // Начальный статус
        notification.setRecipient(request.getTo());
        notification.setSubject(request.getSubject());
        notification.setMessage(request.getMessage());
        notification.setTemplateId(request.getTemplateId());
        notification.setPriority(request.getPriority() != null ? request.getPriority().name() : "NORMAL");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private Notification createPushNotification(PushRequest request) {
        Notification notification = new Notification();
        notification.setId(generateId());
        notification.setType("PUSH");
        notification.setStatus("PENDING"); // Начальный статус
        notification.setRecipient(request.getUserId());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setPriority(request.getPriority() != null ? request.getPriority().name() : "NORMAL");
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}
