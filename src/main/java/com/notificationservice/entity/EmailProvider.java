package com.notificationservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "from_email", nullable = false)
    private String fromEmail;

    @Column(name = "from_name")
    private String fromName;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private Integer priority = 1;

    @Column(name = "daily_limit", nullable = false)
    private Integer dailyLimit = 1000;

    @Column(name = "current_usage", nullable = false)
    private Integer currentUsage = 0;

    @Column(name = "max_connection_pool_size")
    private Integer maxConnectionPoolSize = 5;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout = 5000;

    @Column(name = "timeout")
    private Integer timeout = 5000;

    @Column(name = "use_ssl")
    private Boolean useSsl = false;

    @Column(name = "use_tls")
    private Boolean useTls = true;

    @Column(name = "last_used")
    private LocalDateTime lastUsed;

    @Column(name = "last_reset")
    private LocalDateTime lastReset;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @PrePersist
    public void setInitialValues() {
        if (this.lastReset == null) {
            this.lastReset = LocalDateTime.now();
        }
        validate();
    }

    @PreUpdate
    public void validate() {
        if (priority < 1) {
            throw new IllegalArgumentException("Priority must be at least 1");
        }
        if (dailyLimit < 0) {
            throw new IllegalArgumentException("Daily limit cannot be negative");
        }
        if (currentUsage < 0) {
            throw new IllegalArgumentException("Current usage cannot be negative");
        }
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
    }

    public boolean isAvailable() {
        return active && currentUsage < dailyLimit;
    }

    public double getUsagePercentage() {
        if (dailyLimit == 0) return 0.0;
        return (currentUsage * 100.0) / dailyLimit;
    }

    public boolean needsReset() {
        return lastReset == null || lastReset.toLocalDate().isBefore(LocalDateTime.now().toLocalDate());
    }

    public void incrementUsage() {
        this.currentUsage++;
        this.lastUsed = LocalDateTime.now();
    }

    public void resetUsage() {
        this.currentUsage = 0;
        this.lastReset = LocalDateTime.now();
    }
}
