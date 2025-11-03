package com.notificationservice.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OptimizedMetricsServiceTest {

    private MeterRegistry meterRegistry;
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        metricsService = new MetricsService(meterRegistry);
    }

    @Test
    void recordEmailSent_ShouldUseCachedCounter() {
        // When
        metricsService.recordEmailSent();
        int cacheSizeAfterFirstCall = metricsService.getCacheSize();

        metricsService.recordEmailSent(); // Второй вызов должен использовать кэш
        int cacheSizeAfterSecondCall = metricsService.getCacheSize();

        // Then
        assertEquals(2.0, meterRegistry.counter("notification.emails.sent").count());
        assertEquals(cacheSizeAfterFirstCall, cacheSizeAfterSecondCall); // Cache size should not change
    }

    @Test
    void recordMultipleMetrics_ShouldCacheAllCounters() {
        // When
        metricsService.recordEmailSent();
        metricsService.recordEmailFailed();
        metricsService.recordPushSent();
        metricsService.recordPushFailed();

        // Then - проверяем что все счетчики созданы и работают
        assertEquals(1.0, meterRegistry.counter("notification.emails.sent").count());
        assertEquals(1.0, meterRegistry.counter("notification.emails.failed").count());
        assertEquals(1.0, meterRegistry.counter("notification.push.sent").count());
        assertEquals(1.0, meterRegistry.counter("notification.push.failed").count());

        // Проверяем что кэш содержит все счетчики
        assertEquals(4, metricsService.getCacheSize());
    }

    @Test
    void clearCache_ShouldResetCache() {
        // Given
        metricsService.recordEmailSent(); // Создает и кэширует счетчик
        int initialCacheSize = metricsService.getCacheSize();

        // When
        metricsService.clearCache();
        int cacheSizeAfterClear = metricsService.getCacheSize();

        // Then
        assertEquals(0, cacheSizeAfterClear);
        assertTrue(initialCacheSize > 0, "Initial cache should not be empty");

        // Должен создать счетчик заново, но счетчик в MeterRegistry продолжает существовать
        metricsService.recordEmailSent();
        // 1 вызов до очистки + 1 после = 2
        assertEquals(2.0, meterRegistry.counter("notification.emails.sent").count());
        assertEquals(1, metricsService.getCacheSize());
    }

    @Test
    void recordEmailProcessingTime_ShouldRecordTimer() {
        // When
        metricsService.recordEmailProcessingTime(150, TimeUnit.MILLISECONDS);

        // Then - проверяем что таймер создан и вызван
        long count = meterRegistry.find("notification.email.processing.time").timer().count();
        assertEquals(1, count);
    }
}
