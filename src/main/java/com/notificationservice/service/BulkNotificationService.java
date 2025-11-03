package com.notificationservice.service;

import com.notificationservice.dto.BulkEmailRequest;
import com.notificationservice.dto.EmailRequest;
import com.notificationservice.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class BulkNotificationService {

    private final NotificationService notificationService;

    public List<NotificationResponse> sendBulkEmails(BulkEmailRequest request) {
        log.info("Processing bulk email request for {} recipients", request.getEmails().size());

        return request.getEmails().stream()
                .map(emailRequest -> {
                    // Устанавливаем приоритет из bulk запроса в каждый отдельный email
                    emailRequest.setPriority(convertPriority(request.getPriority()));
                    return notificationService.sendEmail(emailRequest);
                })
                .toList();
    }

    public CompletableFuture<List<NotificationResponse>> sendBulkEmailsAsync(BulkEmailRequest request) {
        log.info("Processing async bulk email request for {} recipients", request.getEmails().size());

        List<CompletableFuture<NotificationResponse>> futures = request.getEmails().stream()
                .map(emailRequest -> {
                    emailRequest.setPriority(convertPriority(request.getPriority()));
                    return notificationService.sendEmailAsync(emailRequest);
                })
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    private EmailRequest.Priority convertPriority(BulkEmailRequest.Priority bulkPriority) {
        return switch (bulkPriority) {
            case LOW -> EmailRequest.Priority.LOW;
            case NORMAL -> EmailRequest.Priority.NORMAL;
            case HIGH -> EmailRequest.Priority.HIGH;
        };
    }
}
