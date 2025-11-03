package com.notificationservice.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    private final WebRequest webRequest = mock(WebRequest.class);

    @Test
    void handleNotificationNotFoundException_ShouldReturnNotFoundResponse() {
        // Arrange
        NotificationNotFoundException exception = new NotificationNotFoundException("Notification 123 not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/notifications/123");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleNotificationNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Notification Not Found", errorResponse.getError());
        assertEquals("Notification 123 not found", errorResponse.getMessage());
        assertEquals("/api/notifications/123", errorResponse.getPath());
        assertNotNull(errorResponse.getTimestamp());
    }

    @Test
    void handleNotificationException_ShouldReturnBadRequestResponse() {
        // Arrange
        NotificationException exception = new NotificationException("Invalid notification data");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleNotificationException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Notification Error", errorResponse.getError());
        assertEquals("Invalid notification data", errorResponse.getMessage());
    }

    @Test
    void handleTemplateNotFoundException_ShouldReturnNotFoundResponse() {
        // Arrange
        TemplateNotFoundException exception = new TemplateNotFoundException("Template welcome-email not found");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleTemplateNotFoundException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.NOT_FOUND.value(), errorResponse.getStatus());
        assertEquals("Template Not Found", errorResponse.getError());
        assertEquals("Template welcome-email not found", errorResponse.getMessage());
    }

    @Test
    void handleEmailSendingException_ShouldReturnInternalServerErrorResponse() {
        // Arrange
        EmailSendingException exception = new EmailSendingException("SMTP connection failed");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleEmailSendingException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Email Sending Failed", errorResponse.getError());
        assertEquals("SMTP connection failed", errorResponse.getMessage());
    }

    @Test
    void handleValidationExceptions_ShouldReturnBadRequestWithDetails() {
        // Arrange
        BindingResult bindingResult = mock(BindingResult.class);
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);

        FieldError fieldError1 = new FieldError("emailRequest", "to", "Email is required");
        FieldError fieldError2 = new FieldError("emailRequest", "subject", "Subject cannot be empty");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleValidationExceptions(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Validation Failed", errorResponse.getError());
        assertEquals("Request validation failed", errorResponse.getMessage());

        assertNotNull(errorResponse.getDetails());
        assertEquals(2, errorResponse.getDetails().size());
        assertEquals("Email is required", errorResponse.getDetails().get("to"));
        assertEquals("Subject cannot be empty", errorResponse.getDetails().get("subject"));
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturnBadRequest() {
        // Arrange
        org.springframework.http.converter.HttpMessageNotReadableException exception =
                new org.springframework.http.converter.HttpMessageNotReadableException("Invalid JSON");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleHttpMessageNotReadableException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.BAD_REQUEST.value(), errorResponse.getStatus());
        assertEquals("Invalid Request", errorResponse.getError());
        assertEquals("Request body is invalid or missing", errorResponse.getMessage());
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerErrorResponse() {
        // Arrange
        Exception exception = new RuntimeException("Unexpected database error");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse.getStatus());
        assertEquals("Internal Server Error", errorResponse.getError());
        assertEquals("An unexpected error occurred", errorResponse.getMessage());
    }

    @Test
    void handleNullPointerException_ShouldReturnInternalServerError() {
        // Arrange
        NullPointerException exception = new NullPointerException("Something was null");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/notifications/push");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleGlobalException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals("Internal Server Error", errorResponse.getError());
    }

    @Test
    void handleHttpMediaTypeNotSupportedException_ShouldReturnUnsupportedMediaType() {
        // Arrange
        org.springframework.web.HttpMediaTypeNotSupportedException exception =
                new org.springframework.web.HttpMediaTypeNotSupportedException("Content-Type 'application/octet-stream' is not supported");
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/notifications/email");

        // Act
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                exceptionHandler.handleHttpMediaTypeNotSupportedException(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, response.getStatusCode());

        GlobalExceptionHandler.ErrorResponse errorResponse = response.getBody();
        assertNotNull(errorResponse);
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), errorResponse.getStatus());
        assertEquals("Unsupported Media Type", errorResponse.getError());
        assertEquals("Content type is not supported", errorResponse.getMessage());
    }

    @Test
    void errorResponse_ShouldHaveCorrectGettersAndSetters() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = Map.of("field", "error message");

        GlobalExceptionHandler.ErrorResponse errorResponse =
                GlobalExceptionHandler.ErrorResponse.builder()
                        .timestamp(timestamp)
                        .status(400)
                        .error("Bad Request")
                        .message("Invalid input")
                        .path("/test")
                        .details(details)
                        .build();

        // Act & Assert
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid input", errorResponse.getMessage());
        assertEquals("/test", errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());

        // Test setters
        LocalDateTime newTimestamp = LocalDateTime.now().plusHours(1);
        Map<String, String> newDetails = Map.of("newField", "new error message");

        errorResponse.setTimestamp(newTimestamp);
        errorResponse.setStatus(404);
        errorResponse.setError("Not Found");
        errorResponse.setMessage("Resource not found");
        errorResponse.setPath("/new-path");
        errorResponse.setDetails(newDetails);

        assertEquals(newTimestamp, errorResponse.getTimestamp());
        assertEquals(404, errorResponse.getStatus());
        assertEquals("Not Found", errorResponse.getError());
        assertEquals("Resource not found", errorResponse.getMessage());
        assertEquals("/new-path", errorResponse.getPath());
        assertEquals(newDetails, errorResponse.getDetails());
    }

    @Test
    void errorResponse_NoArgsConstructor_ShouldWork() {
        // Arrange & Act
        GlobalExceptionHandler.ErrorResponse errorResponse = new GlobalExceptionHandler.ErrorResponse();

        // Assert
        assertNotNull(errorResponse);
        assertNull(errorResponse.getTimestamp());
        assertEquals(0, errorResponse.getStatus());
        assertNull(errorResponse.getError());
        assertNull(errorResponse.getMessage());
        assertNull(errorResponse.getPath());
        assertNull(errorResponse.getDetails());
    }

    @Test
    void errorResponse_AllArgsConstructor_ShouldWork() {
        // Arrange
        LocalDateTime timestamp = LocalDateTime.now();
        Map<String, String> details = Map.of("test", "value");

        // Act
        GlobalExceptionHandler.ErrorResponse errorResponse =
                new GlobalExceptionHandler.ErrorResponse(timestamp, 400, "Error", "Message", "/path", details);

        // Assert
        assertNotNull(errorResponse);
        assertEquals(timestamp, errorResponse.getTimestamp());
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Error", errorResponse.getError());
        assertEquals("Message", errorResponse.getMessage());
        assertEquals("/path", errorResponse.getPath());
        assertEquals(details, errorResponse.getDetails());
    }
}
