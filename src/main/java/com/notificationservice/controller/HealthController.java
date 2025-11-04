package com.notificationservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.Status;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/manage/health")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    private final org.springframework.boot.actuate.health.HealthEndpoint healthEndpoint;

    @GetMapping
    @Operation(summary = "Get application health status")
    public ResponseEntity<Map<String, Object>> getHealth() {
        try {
            HealthComponent healthComponent = healthEndpoint.health();
            Map<String, Object> response = new HashMap<>();
            response.put("status", healthComponent.getStatus().getCode());
            response.put("service", "notification-service");
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            if (healthComponent.getStatus() == Status.UP) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(503).body(errorResponse);
        }
    }

    @GetMapping("/readiness")
    @Operation(summary = "Get application readiness status")
    public ResponseEntity<Map<String, Object>> getReadiness() {
        return getHealthStatus("readiness");
    }

    @GetMapping("/liveness")
    @Operation(summary = "Get application liveness status")
    public ResponseEntity<Map<String, Object>> getLiveness() {
        return getHealthStatus("liveness");
    }

    private ResponseEntity<Map<String, Object>> getHealthStatus(String probeType) {
        try {
            HealthComponent healthComponent;
            try {
                healthComponent = healthEndpoint.healthForPath(probeType);
            } catch (Exception e) {
                // If specific probe is not available, use general health
                healthComponent = healthEndpoint.health();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("status", healthComponent.getStatus().getCode());
            response.put("type", probeType);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            if (healthComponent.getStatus() == Status.UP) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(503).body(response);
            }
        } catch (Exception e) {
            log.error("{} probe failed", probeType, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "DOWN");
            errorResponse.put("type", probeType);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now().toString());
            return ResponseEntity.status(503).body(errorResponse);
        }
    }

    @GetMapping("/info")
    @Operation(summary = "Get application info")
    public ResponseEntity<Map<String, String>> getInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("name", "Notification Service");
        info.put("version", "1.0.0");
        info.put("status", "operational");
        info.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(info);
    }

    @GetMapping("/simple")
    @Operation(summary = "Simple health check")
    public ResponseEntity<Map<String, String>> simpleHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "notification-service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }
}
