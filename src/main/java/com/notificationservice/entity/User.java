package com.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true)
    private String email;

    @Column(name = "push_token", length = 500)
    private String pushToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "device_id")
    private String deviceId;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "preferences", columnDefinition = "TEXT")
    private String preferences; // JSON string for user preferences

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "language")
    private String language = "en";

    @Column(name = "last_notification_at")
    private LocalDateTime lastNotificationAt;

    @Column(name = "notification_count")
    private Integer notificationCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Version
    private Long version;

    public enum Platform {
        IOS("iOS"),
        ANDROID("Android"),
        WEB("Web"),
        UNKNOWN("Unknown");

        private final String displayName;

        Platform(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Platform fromString(String platform) {
            if (platform == null) return UNKNOWN;

            try {
                return Platform.valueOf(platform.toUpperCase());
            } catch (IllegalArgumentException e) {
                return UNKNOWN;
            }
        }
    }

    @PrePersist
    @PreUpdate
    public void validate() {
        if (platform == null) {
            platform = Platform.UNKNOWN;
        }
        if (notificationCount == null) {
            notificationCount = 0;
        }
        if (language == null) {
            language = "en";
        }
    }

    public boolean canReceivePush() {
        return active && pushToken != null && !pushToken.trim().isEmpty();
    }

    public boolean canReceiveEmail() {
        return active && email != null && !email.trim().isEmpty();
    }

    public void incrementNotificationCount() {
        if (this.notificationCount == null) {
            this.notificationCount = 0;
        }
        this.notificationCount++;
        this.lastNotificationAt = LocalDateTime.now();
    }

    public void updateLastSeen() {
        this.lastSeenAt = LocalDateTime.now();
    }

    public boolean isRecentlyActive() {
        return lastSeenAt != null &&
                lastSeenAt.isAfter(LocalDateTime.now().minusDays(7));
    }

    public String getPlatformDisplayName() {
        return platform.getDisplayName();
    }

    // Helper methods for preferences (assuming JSON format)
    public void setEmailPreference(boolean enabled) {
        // Implementation for updating email preferences in JSON
    }

    public void setPushPreference(boolean enabled) {
        // Implementation for updating push preferences in JSON
    }

    public boolean getEmailPreference() {
        // Default to true if preferences not set
        return true;
    }

    public boolean getPushPreference() {
        // Default to true if preferences not set
        return true;
    }
}
