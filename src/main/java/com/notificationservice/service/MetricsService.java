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
    private final ConcurrentMap<String, Timer> timersCache = new ConcurrentHashMap<>();

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
        getOrCreateTimer("notification.email.processing.time").record(duration, unit);
    }

    public void recordPushProcessingTime(long duration, TimeUnit unit) {
        getOrCreateTimer("notification.push.processing.time").record(duration, unit);
    }

    // Notification status metrics with tags
    public void recordNotificationStatus(String type, String status) {
        String cacheKey = String.format("notification.status.type.%s.status.%s", type, status);
        getOrCreateCounterWithTags("notification.status", cacheKey,
                new String[]{"type", type, "status", status}).increment();
    }

    // Generic counter for any notification type and status
    public void recordNotificationMetric(String type, String status) {
        String counterName = String.format("notification.%s.%s", type.toLowerCase(), status.toLowerCase());
        getOrCreateCounter(counterName).increment();
    }

    // Bulk notification metrics
    public void recordBulkNotificationProcessed(String type, int count) {
        String cacheKey = String.format("notification.bulk.processed.type.%s", type);
        getOrCreateCounterWithTags("notification.bulk.processed", cacheKey,
                new String[]{"type", type}).increment(count);
    }

    // Retry metrics
    public void recordNotificationRetry(String type) {
        String cacheKey = String.format("notification.retry.count.type.%s", type);
        getOrCreateCounterWithTags("notification.retry.count", cacheKey,
                new String[]{"type", type}).increment();
    }

    private Counter getOrCreateCounter(String name) {
        return countersCache.computeIfAbsent(name, key ->
                Counter.builder(key)
                        .register(meterRegistry)
        );
    }

    private Counter getOrCreateCounterWithTags(String baseName, String cacheKey, String... tags) {
        return countersCache.computeIfAbsent(cacheKey, key -> {
            Counter.Builder builder = Counter.builder(baseName);
            for (int i = 0; i < tags.length; i += 2) {
                builder.tag(tags[i], tags[i + 1]);
            }
            return builder.register(meterRegistry);
        });
    }

    private Timer getOrCreateTimer(String name) {
        return timersCache.computeIfAbsent(name, key ->
                Timer.builder(key)
                        .register(meterRegistry)
        );
    }

    // Методы для тестирования
    public void clearCache() {
        countersCache.clear();
        timersCache.clear();
    }

    public int getCacheSize() {
        return countersCache.size() + timersCache.size();
    }
}
