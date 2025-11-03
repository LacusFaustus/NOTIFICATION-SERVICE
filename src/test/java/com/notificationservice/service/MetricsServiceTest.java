package com.notificationservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class MetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void recordEmailSent_ShouldIncrementCounter() {
        // When
        metricsService.recordEmailSent();
        metricsService.recordEmailSent();

        // Then
        double count = meterRegistry.counter("notification.emails.sent").count();
        assertEquals(2.0, count);
    }

    @Test
    void recordEmailFailed_ShouldIncrementCounter() {
        // When
        metricsService.recordEmailFailed();

        // Then
        double count = meterRegistry.counter("notification.emails.failed").count();
        assertEquals(1.0, count);
    }

    @Test
    void recordPushSent_ShouldIncrementCounter() {
        // When
        metricsService.recordPushSent();
        metricsService.recordPushSent();
        metricsService.recordPushSent();

        // Then
        double count = meterRegistry.counter("notification.push.sent").count();
        assertEquals(3.0, count);
    }

    @Test
    void recordPushFailed_ShouldIncrementCounter() {
        // When
        metricsService.recordPushFailed();

        // Then
        double count = meterRegistry.counter("notification.push.failed").count();
        assertEquals(1.0, count);
    }

    @Test
    void recordEmailProcessingTime_ShouldRecordTimer() {
        // When
        metricsService.recordEmailProcessingTime(150, TimeUnit.MILLISECONDS);
        metricsService.recordEmailProcessingTime(200, TimeUnit.MILLISECONDS);

        // Then
        long count = meterRegistry.timer("notification.email.processing.time").count();
        assertEquals(2, count);
    }

    @Test
    void recordPushProcessingTime_ShouldRecordTimer() {
        // When
        metricsService.recordPushProcessingTime(100, TimeUnit.MILLISECONDS);

        // Then
        long count = meterRegistry.timer("notification.push.processing.time").count();
        assertEquals(1, count);
    }

    @Test
    void recordNotificationStatus_ShouldCreateCounterWithTags() {
        // When
        metricsService.recordNotificationStatus("EMAIL", "SUCCESS");
        metricsService.recordNotificationStatus("EMAIL", "FAILED");
        metricsService.recordNotificationStatus("PUSH", "SUCCESS");

        // Then
        double emailSuccess = meterRegistry.counter("notification.status",
                "type", "EMAIL", "status", "SUCCESS").count();
        double emailFailed = meterRegistry.counter("notification.status",
                "type", "EMAIL", "status", "FAILED").count();
        double pushSuccess = meterRegistry.counter("notification.status",
                "type", "PUSH", "status", "SUCCESS").count();

        assertEquals(1.0, emailSuccess);
        assertEquals(1.0, emailFailed);
        assertEquals(1.0, pushSuccess);
    }

    @Test
    void recordNotificationMetric_ShouldCreateSpecificCounter() {
        // When
        metricsService.recordNotificationMetric("EMAIL", "success");
        metricsService.recordNotificationMetric("EMAIL", "success");
        metricsService.recordNotificationMetric("PUSH", "failed");

        // Then
        double emailSuccess = meterRegistry.counter("notification.email.success").count();
        double pushFailed = meterRegistry.counter("notification.push.failed").count();

        assertEquals(2.0, emailSuccess);
        assertEquals(1.0, pushFailed);
    }

    @Test
    void recordBulkNotificationProcessed_ShouldRecordWithCount() {
        // When
        metricsService.recordBulkNotificationProcessed("EMAIL", 5);
        metricsService.recordBulkNotificationProcessed("PUSH", 3);

        // Then
        double emailCount = meterRegistry.counter("notification.bulk.processed",
                "type", "EMAIL").count();
        double pushCount = meterRegistry.counter("notification.bulk.processed",
                "type", "PUSH").count();

        assertEquals(5.0, emailCount);
        assertEquals(3.0, pushCount);
    }

    @Test
    void recordNotificationRetry_ShouldIncrementRetryCounter() {
        // When
        metricsService.recordNotificationRetry("EMAIL");
        metricsService.recordNotificationRetry("EMAIL");
        metricsService.recordNotificationRetry("PUSH");

        // Then
        double emailRetry = meterRegistry.counter("notification.retry.count",
                "type", "EMAIL").count();
        double pushRetry = meterRegistry.counter("notification.retry.count",
                "type", "PUSH").count();

        assertEquals(2.0, emailRetry);
        assertEquals(1.0, pushRetry);
    }

    @Test
    void clearCache_ShouldResetCache() {
        // Given
        metricsService.recordEmailSent(); // Populates cache
        int initialCacheSize = metricsService.getCacheSize();

        // When
        metricsService.clearCache();
        int cacheSizeAfterClear = metricsService.getCacheSize();

        // Then
        assertEquals(0, cacheSizeAfterClear);
        assertTrue(initialCacheSize > 0, "Initial cache should not be empty");

        // Should work after cache clear
        // Счетчик продолжает существовать в MeterRegistry, поэтому значения накапливаются
        metricsService.recordEmailSent();
        // 1 вызов до очистки + 1 после = 2
        double count = meterRegistry.counter("notification.emails.sent").count();
        assertEquals(2.0, count);
        assertEquals(1, metricsService.getCacheSize());
    }
}
