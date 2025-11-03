package com.notificationservice.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExceptionTestUtils {

    public static MethodArgumentNotValidException createValidationException(String fieldName, String errorMessage) {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("objectName", fieldName, errorMessage);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError)); // Используем getFieldErrors вместо getAllErrors
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        return exception;
    }

    public static MethodArgumentNotValidException createMultipleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("emailRequest", "to", "Email is required");
        FieldError fieldError2 = new FieldError("emailRequest", "subject", "Subject cannot be empty");
        FieldError fieldError3 = new FieldError("emailRequest", "message", "Message is required");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2, fieldError3));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        return exception;
    }
}
