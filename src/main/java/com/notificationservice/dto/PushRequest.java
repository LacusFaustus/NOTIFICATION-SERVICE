package com.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PushRequest {

    public enum Priority {
        LOW, NORMAL, HIGH
    }

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Priority is required")
    private Priority priority = Priority.NORMAL;

    @Pattern(regexp = "ANDROID|IOS|ALL", message = "Platform must be ANDROID, IOS or ALL")
    private String platform = "ALL";
}
