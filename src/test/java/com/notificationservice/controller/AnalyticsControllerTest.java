package com.notificationservice.controller;

import com.notificationservice.config.TestSecurityConfig;
import com.notificationservice.dto.NotificationStats;
import com.notificationservice.service.AnalyticsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import(TestSecurityConfig.class)
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

    @Test
    void getStats_ShouldReturnStatistics() throws Exception {
        // Arrange
        NotificationStats stats = new NotificationStats();
        stats.setTotalNotifications(100);
        stats.setSuccessRate(95.0);
        stats.setNotificationsByType(Map.of("EMAIL", 80L, "PUSH", 20L));
        stats.setNotificationsByStatus(Map.of("SENT", 95L, "FAILED", 5L));

        when(analyticsService.getNotificationStats(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/stats")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalNotifications").value(100))
                .andExpect(jsonPath("$.successRate").value(95.0));
    }

    @Test
    void getAverageProcessingTime_ShouldReturnValue() throws Exception {
        // Arrange
        when(analyticsService.getAverageProcessingTime()).thenReturn(150.0);

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/avg-processing-time"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(150.0));
    }

    @Test
    void getDailyCount_ShouldReturnOk() throws Exception {
        // Arrange
        when(analyticsService.getDailyNotificationCount(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Map.of(LocalDate.now(), 10L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/daily-count")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk());
    }

    @Test
    void getTopRecipients_ShouldReturnOk() throws Exception {
        // Arrange
        when(analyticsService.getTopRecipients(any(Integer.class)))
                .thenReturn(Map.of("test@example.com", 5L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/top-recipients")
                        .param("limit", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void getFailureReasons_ShouldReturnOk() throws Exception {
        // Arrange
        when(analyticsService.getFailureReasons())
                .thenReturn(Map.of("SMTP Error", 3L));

        // Act & Assert
        mockMvc.perform(get("/api/v1/analytics/failure-reasons"))
                .andExpect(status().isOk());
    }
}
