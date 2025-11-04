package com.notificationservice.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@TestConfiguration
@EnableAutoConfiguration
@EnableJpaRepositories(basePackages = "com.notificationservice.repository")
@EnableAsync
@Import({TestSecurityConfig.class, TestConfig.class})
@ComponentScan(
        basePackages = "com.notificationservice",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.notificationservice\\.config\\.MonitoringConfig"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.notificationservice\\.config\\.RabbitMQConfig"),
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com\\.notificationservice\\.config\\.JwtConfig")
        }
)
public class TestApplicationConfig {
    // Конфигурация для тестов - исключаем проблемные конфигурации
}
