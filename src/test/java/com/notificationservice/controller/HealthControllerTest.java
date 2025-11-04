package com.notificationservice.controller;

import com.notificationservice.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HealthEndpoint healthEndpoint;

    @Test
    void getSimpleHealth_ShouldReturnOk() throws Exception {
        mockMvc.perform(get("/manage/health/simple"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("notification-service"));
    }

    @Test
    void getInfo_ShouldReturnApplicationInfo() throws Exception {
        mockMvc.perform(get("/manage/health/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Notification Service"))
                .andExpect(jsonPath("$.version").value("1.0.0"));
    }

    @Test
    void getHealth_ShouldReturnOk() throws Exception {
        // Mock health endpoint to return UP status
        Health health = Health.up()
                .withDetail("database", "up")
                .withDetail("redis", "up")
                .build();

        when(healthEndpoint.health()).thenReturn(health);

        mockMvc.perform(get("/manage/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getReadiness_ShouldReturnOk() throws Exception {
        // Mock readiness health check
        Health readinessHealth = Health.up()
                .withDetail("readiness", "ready")
                .build();

        when(healthEndpoint.healthForPath("readiness")).thenReturn(readinessHealth);

        mockMvc.perform(get("/manage/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getLiveness_ShouldReturnOk() throws Exception {
        // Mock liveness health check
        Health livenessHealth = Health.up()
                .withDetail("liveness", "alive")
                .build();

        when(healthEndpoint.healthForPath("liveness")).thenReturn(livenessHealth);

        mockMvc.perform(get("/manage/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void getHealth_WhenServiceIsDown_ShouldReturnServiceUnavailable() throws Exception {
        // Mock health endpoint to return DOWN status
        Health health = Health.down()
                .withDetail("database", "down")
                .build();

        when(healthEndpoint.health()).thenReturn(health);

        mockMvc.perform(get("/manage/health"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }
}
