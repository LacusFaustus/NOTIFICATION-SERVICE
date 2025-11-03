package com.notificationservice.exception;

import java.io.Serial;

public class EmailSendingException extends NotificationException {

    @Serial
    private static final long serialVersionUID = 1012320433731299010L;

    public EmailSendingException(String message) {
        super(message);
    }

    public EmailSendingException(String message, Throwable cause) {
        super(message, cause);
    }
}
