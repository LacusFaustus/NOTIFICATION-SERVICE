package com.notificationservice.integration;

import com.notificationservice.NotificationServiceApplication;
import com.notificationservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = NotificationServiceApplication.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class SimpleIntegrationTest {

    @Test
    void contextLoads() {
        // Simple test to verify Spring context loads
        assertTrue(true);
    }

    @Test
    void applicationStarts() {
        // Verify the application can start
        assertTrue(true);
    }
}
