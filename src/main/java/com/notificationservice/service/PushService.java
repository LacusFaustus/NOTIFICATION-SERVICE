package com.notificationservice.service;

import com.notificationservice.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PushService {

    private final MetricsService metricsService;

    public void sendPush(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException("Notification cannot be null");
        }

        try {
            // Имитация отправки push-уведомления
            log.info("Sending push notification to user: {}, title: {}, message: {}",
                    notification.getRecipient(),
                    notification.getTitle(), // Теперь это поле существует
                    notification.getMessage());

            // В реальной реализации здесь был бы вызов внешнего сервиса
            // Например: firebaseService.sendPush(notification);

            // Имитация задержки сети
            Thread.sleep(100);

            log.info("Push notification sent successfully to user: {}", notification.getRecipient());

        } catch (Exception e) {
            log.error("Failed to send push notification to user: {}", notification.getRecipient(), e);
            metricsService.recordPushFailed();
            throw new RuntimeException("Push notification sending failed: " + e.getMessage(), e);
        }
    }
}
