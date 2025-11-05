package com.notificationservice.service;

import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetryService {

    private final NotificationRepository notificationRepository;
    private final RabbitTemplate rabbitTemplate;
    private final NotificationService notificationService;

    @Value("${notification.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${notification.retry.backoff-delay:1000}")
    private long backoffDelay;

    public void retryFailedNotification(String notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isEmpty()) {
            log.warn("Cannot retry notification: notification not found with id: {}", notificationId);
            return;
        }

        Notification notification = notificationOpt.get();

        if (notification.getRetryCount() >= maxRetryAttempts) {
            log.warn("Notification {} has reached maximum retry attempts ({}), moving to DLQ",
                    notificationId, maxRetryAttempts);
            moveToDeadLetterQueue(notification);
            return;
        }

        try {
            // Apply exponential backoff
            long delay = calculateBackoffDelay(notification.getRetryCount());
            Thread.sleep(delay);

            log.info("Retrying notification {} (attempt {})",
                    notificationId, notification.getRetryCount() + 1);

            notificationService.processNotification(notificationId);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Retry interrupted for notification {}", notificationId);
        } catch (Exception e) {
            log.error("Retry failed for notification {}: {}", notificationId, e.getMessage());
            incrementRetryCount(notification);
        }
    }

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void retryStuckNotifications() {
        log.info("Checking for stuck notifications to retry...");

        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(10);
        List<Notification> stuckNotifications = notificationRepository
                .findByStatusAndCreatedAtBefore("PENDING", cutoffTime);

        for (Notification notification : stuckNotifications) {
            log.info("Retrying stuck notification: {}", notification.getId());
            rabbitTemplate.convertAndSend(
                    "notification.exchange",
                    "notification.routing.key",
                    notification.getId()
            );
        }

        if (!stuckNotifications.isEmpty()) {
            log.info("Retried {} stuck notifications", stuckNotifications.size());
        }
    }

    public List<Notification> getNotificationsForRetry() {
        return notificationRepository.findByStatusAndRetryCountLessThan("FAILED", maxRetryAttempts);
    }

    private long calculateBackoffDelay(int retryCount) {
        return (long) (backoffDelay * Math.pow(2, retryCount));
    }

    private void incrementRetryCount(Notification notification) {
        notification.setRetryCount(notification.getRetryCount() + 1);
        notificationRepository.save(notification);
    }

    private void moveToDeadLetterQueue(Notification notification) {
        notification.setStatus("FAILED_PERMANENTLY");
        notificationRepository.save(notification);

        // Send to DLQ for manual processing
        rabbitTemplate.convertAndSend(
                "notification.dlq.exchange",
                "notification.dlq.routing.key",
                notification.getId()
        );
    }
}
