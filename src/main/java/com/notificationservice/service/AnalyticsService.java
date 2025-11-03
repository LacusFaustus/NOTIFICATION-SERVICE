package com.notificationservice.service;

import com.notificationservice.dto.NotificationStats;
import com.notificationservice.entity.Notification;
import com.notificationservice.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final NotificationRepository notificationRepository;

    public NotificationStats getNotificationStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<Notification> notifications = notificationRepository.findByCreatedAtBetween(start, end);

        NotificationStats stats = new NotificationStats();
        stats.setTotalNotifications(notifications.size());
        stats.setPeriodStart(start);
        stats.setPeriodEnd(end);

        Map<String, Long> notificationsByType = new HashMap<>();
        Map<String, Long> notificationsByStatus = new HashMap<>();

        for (Notification notification : notifications) {
            // Count by type
            notificationsByType.merge(notification.getType(), 1L, Long::sum);

            // Count by status
            notificationsByStatus.merge(notification.getStatus(), 1L, Long::sum);
        }

        stats.setNotificationsByType(notificationsByType);
        stats.setNotificationsByStatus(notificationsByStatus);

        // Calculate success rate
        long successful = notificationsByStatus.getOrDefault("SENT", 0L);
        long failed = notificationsByStatus.getOrDefault("FAILED", 0L);
        long total = successful + failed;

        if (total > 0) {
            stats.setSuccessRate((double) successful / total * 100);
        } else {
            stats.setSuccessRate(0.0);
        }

        // Calculate average processing time
        Double avgTime = notificationRepository.findAverageProcessingTime();
        stats.setAverageProcessingTimeMs(avgTime != null ? (long) (avgTime * 1000) : 0L);

        return stats;
    }

    public Map<LocalDate, Long> getDailyNotificationCount(LocalDate startDate, LocalDate endDate) {
        List<Object[]> results = notificationRepository.getDailyCountByDateRange(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
        );

        Map<LocalDate, Long> dailyCounts = new HashMap<>();
        for (Object[] result : results) {
            LocalDate date = ((java.sql.Date) result[0]).toLocalDate();
            Long count = ((Long) result[1]);
            dailyCounts.put(date, count);
        }

        // Fill missing dates with zero
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            dailyCounts.putIfAbsent(current, 0L);
            current = current.plusDays(1);
        }

        return dailyCounts;
    }

    public Map<String, Long> getTopRecipients(int limit) {
        List<Object[]> results = notificationRepository.findTopRecipients(limit);
        Map<String, Long> topRecipients = new HashMap<>();

        for (Object[] result : results) {
            String recipient = (String) result[0];
            Long count = (Long) result[1];
            topRecipients.put(recipient, count);
        }

        return topRecipients;
    }

    public Map<String, Long> getFailureReasons() {
        List<Object[]> results = notificationRepository.findFailureReasons();
        Map<String, Long> failureReasons = new HashMap<>();

        for (Object[] result : results) {
            String errorMessage = (String) result[0];
            Long count = (Long) result[1];
            failureReasons.put(errorMessage != null ? errorMessage : "Unknown error", count);
        }

        return failureReasons;
    }

    public double getAverageProcessingTime() {
        Double avgTime = notificationRepository.findAverageProcessingTime();
        return avgTime != null ? avgTime : 0.0;
    }

    // Дополнительные методы аналитики
    public Map<String, Long> getNotificationsByType() {
        List<Object[]> results = notificationRepository.countByType();
        Map<String, Long> byType = new HashMap<>();

        for (Object[] result : results) {
            String type = (String) result[0];
            Long count = (Long) result[1];
            byType.put(type, count);
        }

        return byType;
    }

    public Map<String, Long> getNotificationsByStatus() {
        List<Object[]> results = notificationRepository.countByStatusGroup();
        Map<String, Long> byStatus = new HashMap<>();

        for (Object[] result : results) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            byStatus.put(status, count);
        }

        return byStatus;
    }

    public Map<String, Long> getNotificationsByPriority() {
        List<Object[]> results = notificationRepository.countByPriority();
        Map<String, Long> byPriority = new HashMap<>();

        for (Object[] result : results) {
            String priority = (String) result[0];
            Long count = (Long) result[1];
            byPriority.put(priority, count);
        }

        return byPriority;
    }
}
