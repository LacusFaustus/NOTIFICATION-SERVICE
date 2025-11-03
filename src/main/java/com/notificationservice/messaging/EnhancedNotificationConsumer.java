package com.notificationservice.messaging;

import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import com.notificationservice.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Profile("!dev") // Запускается только в НЕ-dev профилях
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedNotificationConsumer {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final PushService pushService;
    private final MetricsService metricsService;

    @RabbitListener(queues = "${rabbitmq.queue.notification:notification.queue}")
    @Transactional
    public void processNotification(String notificationId) {
        long startTime = System.currentTimeMillis();
        String notificationType = "UNKNOWN";

        try {
            log.info("Processing notification: {}", notificationId);

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

            notificationType = notification.getType();

            if (!"PENDING".equals(notification.getStatus())) {
                log.warn("Notification {} is not in PENDING state: {}", notificationId, notification.getStatus());
                return;
            }

            processNotificationInternal(notification);
            metricsService.recordNotificationStatus(notificationType, "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to process notification {}: {}", notificationId, e.getMessage());
            metricsService.recordNotificationStatus(notificationType, "FAILED");
            throw new RuntimeException("Notification processing failed", e);
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            recordProcessingTime(notificationType, processingTime);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.email:email.queue}")
    @Transactional
    public void processEmailNotification(String notificationId) {
        processSpecificNotification(notificationId, "EMAIL");
    }

    @RabbitListener(queues = "${rabbitmq.queue.push:push.queue}")
    @Transactional
    public void processPushNotification(String notificationId) {
        processSpecificNotification(notificationId, "PUSH");
    }

    private void processSpecificNotification(String notificationId, String expectedType) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("Processing {} notification: {}", expectedType, notificationId);

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));

            if (!expectedType.equals(notification.getType())) {
                log.warn("Notification {} is not {} type: {}", notificationId, expectedType, notification.getType());
                return;
            }

            if (!"PENDING".equals(notification.getStatus())) {
                log.warn("Notification {} is not in PENDING state: {}", notificationId, notification.getStatus());
                return;
            }

            processNotificationInternal(notification);
            metricsService.recordNotificationStatus(expectedType, "SUCCESS");

        } catch (Exception e) {
            log.error("Failed to process {} notification {}: {}", expectedType, notificationId, e.getMessage());
            metricsService.recordNotificationStatus(expectedType, "FAILED");
            throw new RuntimeException(expectedType + " notification processing failed", e);
        } finally {
            long processingTime = System.currentTimeMillis() - startTime;
            recordProcessingTime(expectedType, processingTime);
        }
    }

    private void processNotificationInternal(Notification notification) {
        try {
            switch (notification.getType()) {
                case "EMAIL":
                    emailService.sendEmail(notification);
                    metricsService.recordEmailSent();
                    break;
                case "PUSH":
                    pushService.sendPush(notification);
                    metricsService.recordPushSent();
                    break;
                default:
                    throw new RuntimeException("Unknown notification type: " + notification.getType());
            }

            notification.setStatus("SENT");
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Successfully processed {} notification: {}",
                    notification.getType(), notification.getId());

        } catch (Exception e) {
            handleProcessingFailure(notification, e);
            throw e;
        }
    }

    private void handleProcessingFailure(Notification notification, Exception e) {
        notification.setStatus("FAILED");
        notification.setErrorMessage(e.getMessage());
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);

        if ("EMAIL".equals(notification.getType())) {
            metricsService.recordEmailFailed();
            metricsService.recordNotificationRetry("EMAIL");
        } else if ("PUSH".equals(notification.getType())) {
            metricsService.recordPushFailed();
            metricsService.recordNotificationRetry("PUSH");
        }

        log.error("Failed to process {} notification {}: {}",
                notification.getType(), notification.getId(), e.getMessage());
    }

    private void recordProcessingTime(String notificationType, long processingTimeMillis) {
        if ("EMAIL".equals(notificationType)) {
            metricsService.recordEmailProcessingTime(processingTimeMillis, TimeUnit.MILLISECONDS);
        } else if ("PUSH".equals(notificationType)) {
            metricsService.recordPushProcessingTime(processingTimeMillis, TimeUnit.MILLISECONDS);
        }
        log.debug("{} notification processing time: {} ms", notificationType, processingTimeMillis);
    }

    @RabbitListener(queues = "${rabbitmq.queue.dlq:notification.dlq}")
    public void handleDeadLetterMessage(String notificationId) {
        log.error("Received message in DLQ: {}", notificationId);

        try {
            Notification notification = notificationRepository.findById(notificationId).orElse(null);
            if (notification != null) {
                notification.setStatus("FAILED_PERMANENTLY");
                notificationRepository.save(notification);

                metricsService.recordNotificationStatus(notification.getType(), "PERMANENT_FAILURE");
                log.warn("Notification {} permanently failed and moved to DLQ", notificationId);
            }
        } catch (Exception e) {
            log.error("Failed to handle DLQ message {}: {}", notificationId, e.getMessage());
        }
    }
}
