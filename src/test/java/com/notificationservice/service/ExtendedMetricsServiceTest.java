package com.notificationservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedMetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void recordEmailProcessingTime_ShouldRecordTimer() {
        // Given
        long duration = 150L;
        TimeUnit unit = TimeUnit.MILLISECONDS;

        // When
        metricsService.recordEmailProcessingTime(duration, unit);

        // Then
        long count = meterRegistry.timer("notification.email.processing.time").count();
        assertEquals(1, count);
    }

    @Test
    void recordPushProcessingTime_ShouldRecordTimer() {
        // Given
        long duration = 100L;
        TimeUnit unit = TimeUnit.MILLISECONDS;

        // When
        metricsService.recordPushProcessingTime(duration, unit);

        // Then
        long count = meterRegistry.timer("notification.push.processing.time").count();
        assertEquals(1, count);
    }

    @Test
    void recordNotificationStatus_ShouldCreateCounterWithTags() {
        // Given
        String type = "EMAIL";
        String status = "SUCCESS";

        // When
        metricsService.recordNotificationStatus(type, status);

        // Then
        double count = meterRegistry.counter("notification.status",
                "type", type, "status", status).count();
        assertEquals(1.0, count);
    }

    @Test
    void recordNotificationMetric_ShouldCreateSpecificCounter() {
        // Given
        String type = "EMAIL";
        String status = "SUCCESS";

        // When
        metricsService.recordNotificationMetric(type, status);

        // Then
        double count = meterRegistry.counter("notification.email.success").count();
        assertEquals(1.0, count);
    }

    @Test
    void recordBulkNotificationProcessed_ShouldRecordWithCount() {
        // Given
        String type = "EMAIL";
        int count = 5;

        // When
        metricsService.recordBulkNotificationProcessed(type, count);

        // Then
        double actualCount = meterRegistry.counter("notification.bulk.processed",
                "type", type).count();
        assertEquals(5.0, actualCount);
    }

    @Test
    void recordNotificationRetry_ShouldIncrementRetryCounter() {
        // Given
        String type = "EMAIL";

        // When
        metricsService.recordNotificationRetry(type);

        // Then
        double count = meterRegistry.counter("notification.retry.count",
                "type", type).count();
        assertEquals(1.0, count);
    }

    @Test
    void multipleOperations_ShouldUseCache() {
        // When
        metricsService.recordEmailSent();
        int cacheSizeAfterFirstCall = metricsService.getCacheSize();

        metricsService.recordEmailSent(); // Should use cached counter
        int cacheSizeAfterSecondCall = metricsService.getCacheSize();

        // Then
        assertEquals(2.0, meterRegistry.counter("notification.emails.sent").count());
        // Cache size should be the same after multiple calls (reusing cached objects)
        assertEquals(cacheSizeAfterFirstCall, cacheSizeAfterSecondCall);
    }

    @Test
    void clearCache_ShouldResetAllCaches() {
        // Given
        metricsService.recordEmailSent(); // Populates cache - 1 вызов
        metricsService.recordEmailProcessingTime(100, TimeUnit.MILLISECONDS); // Populates cache - 1 вызов
        int initialCacheSize = metricsService.getCacheSize();

        // When
        metricsService.clearCache();
        int cacheSizeAfterClear = metricsService.getCacheSize();

        // Then
        assertEquals(0, cacheSizeAfterClear);
        assertTrue(initialCacheSize > 0, "Initial cache should not be empty");

        // Should work after cache clear - создаем новые счетчики/таймеры
        metricsService.recordEmailSent(); // 2 вызов
        metricsService.recordEmailProcessingTime(200, TimeUnit.MILLISECONDS); // 2 вызов

        // Проверяем что метрики работают после очистки кэша
        // Счетчик: 1 вызов до очистки + 1 после = 2
        assertEquals(2.0, meterRegistry.counter("notification.emails.sent").count());
        // Таймер: 1 вызов до очистки + 1 после = 2
        assertEquals(2, meterRegistry.timer("notification.email.processing.time").count());
        // Новые объекты в кэше
        assertEquals(2, metricsService.getCacheSize());
    }
}
