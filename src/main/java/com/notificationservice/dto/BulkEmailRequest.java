package com.notificationservice.dto;

import lombok.Data;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
public class BulkEmailRequest {

    @NotEmpty(message = "Emails list cannot be empty")
    private List<@Valid EmailRequest> emails;

    @NotNull
    private Priority priority = Priority.NORMAL;

    public enum Priority {
        LOW, NORMAL, HIGH
    }
}
