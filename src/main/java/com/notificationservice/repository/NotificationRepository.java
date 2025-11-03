package com.notificationservice.repository;

import com.notificationservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {

    List<Notification> findByStatus(String status);

    List<Notification> findByRecipient(String recipient);

    List<Notification> findByType(String type);

    long countByStatus(String status);

    List<Notification> findByStatusAndRetryCountLessThan(String status, int maxRetryCount);

    // Методы для AnalyticsService
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(n.processingTime) FROM Notification n WHERE n.processingTime IS NOT NULL")
    Double findAverageProcessingTime();

    @Query("SELECT DATE(n.createdAt) as date, COUNT(n) as count " +
            "FROM Notification n " +
            "WHERE n.createdAt BETWEEN :start AND :end " +
            "GROUP BY DATE(n.createdAt) " +
            "ORDER BY date")
    List<Object[]> getDailyCountByDateRange(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Query("SELECT n.recipient, COUNT(n) as count " +
            "FROM Notification n " +
            "GROUP BY n.recipient " +
            "ORDER BY count DESC " +
            "LIMIT :limit")
    List<Object[]> findTopRecipients(@Param("limit") int limit);

    @Query("SELECT n.errorMessage, COUNT(n) as count " +
            "FROM Notification n " +
            "WHERE n.errorMessage IS NOT NULL " +
            "GROUP BY n.errorMessage " +
            "ORDER BY count DESC")
    List<Object[]> findFailureReasons();

    @Query("SELECT n.type, COUNT(n) as count " +
            "FROM Notification n " +
            "GROUP BY n.type")
    List<Object[]> countByType();

    @Query("SELECT n.status, COUNT(n) as count " +
            "FROM Notification n " +
            "GROUP BY n.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT n.priority, COUNT(n) as count " +
            "FROM Notification n " +
            "WHERE n.priority IS NOT NULL " +
            "GROUP BY n.priority")
    List<Object[]> countByPriority();

    // Метод для RetryService
    List<Notification> findByStatusAndCreatedAtBefore(String status, LocalDateTime dateTime);

}
