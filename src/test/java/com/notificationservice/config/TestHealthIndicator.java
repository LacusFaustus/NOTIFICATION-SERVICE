package com.notificationservice.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        // Всегда возвращаем UP статус в тестах
        return Health.up()
                .withDetail("database", "up")
                .withDetail("redis", "up")
                .withDetail("rabbitmq", "up")
                .withDetail("status", "All systems operational")
                .build();
    }
}
