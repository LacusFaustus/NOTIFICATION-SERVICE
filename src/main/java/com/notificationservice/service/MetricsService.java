package com.notificationservice.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MetricsService {

    private final MeterRegistry meterRegistry;
    private final ConcurrentMap<String, Counter> countersCache = new ConcurrentHashMap<>();

    // Email metrics
    public void recordEmailSent() {
        getOrCreateCounter("notification.emails.sent").increment();
    }

    public void recordEmailFailed() {
        getOrCreateCounter("notification.emails.failed").increment();
    }

    // Push metrics
    public void recordPushSent() {
        getOrCreateCounter("notification.push.sent").increment();
    }

    public void recordPushFailed() {
        getOrCreateCounter("notification.push.failed").increment();
    }

    // Processing time metrics
    public void recordEmailProcessingTime(long duration, TimeUnit unit) {
        Timer.builder("notification.email.processing.time")
                .register(meterRegistry)
                .record(duration, unit);
    }

    public void recordPushProcessingTime(long duration, TimeUnit unit) {
        Timer.builder("notification.push.processing.time")
                .register(meterRegistry)
                .record(duration, unit);
    }

    // Notification status metrics with tags
    public void recordNotificationStatus(String type, String status) {
        Counter.builder("notification.status")
                .tag("type", type)
                .tag("status", status)
                .register(meterRegistry)
                .increment();
    }

    // Retry metrics
    public void recordNotificationRetry(String type) {
        Counter.builder("notification.retry.count")
                .tag("type", type)
                .register(meterRegistry)
                .increment();
    }

    // Generic counter for any notification type and status
    public void recordNotificationMetric(String type, String status) {
        getOrCreateCounter(String.format("notification.%s.%s", type.toLowerCase(), status.toLowerCase()))
                .increment();
    }

    // Bulk notification metrics
    public void recordBulkNotificationProcessed(String type, int count) {
        Counter.builder("notification.bulk.processed")
                .tag("type", type)
                .register(meterRegistry)
                .increment(count);
    }

    private Counter getOrCreateCounter(String name) {
        return countersCache.computeIfAbsent(name, key ->
                Counter.builder(key)
                        .register(meterRegistry)
        );
    }

    // Methods for testing
    public void clearCache() {
        countersCache.clear();
    }

    public int getCacheSize() {
        return countersCache.size();
    }
}
