package com.notificationservice.exception;

import java.io.Serial;

public class NotificationNotFoundException extends NotificationException {

    @Serial
    private static final long serialVersionUID = 7263063305795869240L;

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
