package com.notificationservice.messaging;

import com.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("!dev") // Запускается только в НЕ-dev профилях
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${rabbitmq.queue.notification:notification.queue}")
    public void processNotification(String notificationId) {
        try {
            log.info("Processing notification from queue: {}", notificationId);
            notificationService.processNotification(notificationId);
            log.info("Successfully processed notification: {}", notificationId);
        } catch (Exception e) {
            log.error("Failed to process notification {}: {}", notificationId, e.getMessage());
            throw new RuntimeException("Notification processing failed", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.dlq:notification.dlq}")
    public void processFailedNotification(String notificationId) {
        try {
            log.warn("Processing failed notification from DLQ: {}", notificationId);
            // Additional logic for handling failed notifications
            // Could include sending alerts, logging to external system, etc.
            log.error("Notification {} has failed multiple times and moved to DLQ", notificationId);
        } catch (Exception e) {
            log.error("Failed to process DLQ notification {}: {}", notificationId, e.getMessage());
        }
    }
}
