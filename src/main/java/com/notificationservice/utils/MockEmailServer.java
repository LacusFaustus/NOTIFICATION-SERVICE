package com.notificationservice.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Mock SMTP server for testing email functionality
 * Simplified version that logs emails instead of actually sending them
 */
@Component
@Slf4j
public class MockEmailServer {

    private static final int MAX_EMAILS = 1000;
    private final List<MockEmail> receivedEmails = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;

    @PostConstruct
    public void start() {
        try {
            running = true;
            log.info("Mock email server started (logging mode)");
            log.info("Emails will be logged instead of actually sent");
        } catch (Exception e) {
            log.error("Failed to start mock email server", e);
            throw new RuntimeException("Failed to start mock email server", e);
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        receivedEmails.clear();
        log.info("Mock email server stopped");
    }

    /**
     * Simulate sending an email
     */
    public void sendEmail(String from, String to, String subject, String content) {
        if (!running) {
            throw new IllegalStateException("Mock email server is not running");
        }

        // Limit storage size
        if (receivedEmails.size() >= MAX_EMAILS) {
            receivedEmails.remove(0);
        }

        MockEmail email = new MockEmail(from, to, subject, content);
        receivedEmails.add(email);

        log.info("Mock email sent: From: {}, To: {}, Subject: {}", from, to, subject);
        log.debug("Email content: {}", content);
    }

    /**
     * Get the number of received messages
     */
    public int getReceivedMessagesCount() {
        return receivedEmails.size();
    }

    /**
     * Get all received messages
     */
    public List<MockEmail> getReceivedMessages() {
        return new ArrayList<>(receivedEmails);
    }

    /**
     * Get the last received message
     */
    public MockEmail getLastReceivedMessage() {
        return receivedEmails.isEmpty() ? null : receivedEmails.get(receivedEmails.size() - 1);
    }

    /**
     * Wait for messages to arrive (useful for async testing)
     */
    public void waitForIncomingEmail(int count, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        while (receivedEmails.size() < count && (System.currentTimeMillis() - startTime) < timeoutMillis) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * Reset the server - clear all received messages
     */
    public void reset() {
        receivedEmails.clear();
        log.debug("Mock email server reset");
    }

    /**
     * Check if server is running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Verify that an email was sent to a specific recipient
     */
    public boolean wasEmailSentTo(String recipient) {
        return receivedEmails.stream()
                .anyMatch(email -> email.getTo().equalsIgnoreCase(recipient));
    }

    /**
     * Verify that an email with specific subject was sent
     */
    public boolean wasEmailSentWithSubject(String subject) {
        return receivedEmails.stream()
                .anyMatch(email -> email.getSubject().toLowerCase().contains(subject.toLowerCase()));
    }

    /**
     * Get the content of the last received email
     */
    public String getLastEmailContent() {
        MockEmail lastEmail = getLastReceivedMessage();
        return lastEmail != null ? lastEmail.getContent() : null;
    }

    /**
     * Print all received messages for debugging
     */
    public void printReceivedMessages() {
        log.info("Received {} email(s):", receivedEmails.size());
        for (int i = 0; i < receivedEmails.size(); i++) {
            MockEmail email = receivedEmails.get(i);
            log.info("Email {}: From: {}, To: {}, Subject: {}",
                    i + 1, email.getFrom(), email.getTo(), email.getSubject());
        }
    }

    /**
     * Find emails by criteria
     */
    public List<MockEmail> findEmails(Predicate<MockEmail> criteria) {
        return receivedEmails.stream()
                .filter(criteria)
                .collect(Collectors.toList());
    }

    /**
     * Get email statistics
     */
    public Map<String, Long> getEmailStatistics() {
        return receivedEmails.stream()
                .collect(Collectors.groupingBy(
                        MockEmail::getTo,
                        Collectors.counting()
                ));
    }

    /**
     * Clear emails older than specified time
     */
    public void clearEmailsOlderThan(long maxAgeMillis) {
        long cutoffTime = System.currentTimeMillis() - maxAgeMillis;
        receivedEmails.removeIf(email -> email.getTimestamp() < cutoffTime);
    }

    /**
     * Verify that an email was sent from a specific sender
     */
    public boolean wasEmailSentFrom(String sender) {
        return receivedEmails.stream()
                .anyMatch(email -> email.getFrom().equalsIgnoreCase(sender));
    }

    /**
     * Verify that an email contains specific text in content
     */
    public boolean wasEmailSentWithContent(String contentText) {
        return receivedEmails.stream()
                .anyMatch(email -> email.getContent().toLowerCase().contains(contentText.toLowerCase()));
    }

    /**
     * Get all emails sent to a specific recipient
     */
    public List<MockEmail> getEmailsTo(String recipient) {
        return findEmails(email -> email.getTo().equalsIgnoreCase(recipient));
    }

    /**
     * Get all emails from a specific sender
     */
    public List<MockEmail> getEmailsFrom(String sender) {
        return findEmails(email -> email.getFrom().equalsIgnoreCase(sender));
    }

    /**
     * Get all emails with specific subject
     */
    public List<MockEmail> getEmailsWithSubject(String subject) {
        return findEmails(email -> email.getSubject().toLowerCase().contains(subject.toLowerCase()));
    }

    /**
     * Check if any emails were received
     */
    public boolean hasReceivedEmails() {
        return !receivedEmails.isEmpty();
    }

    /**
     * Clear all emails for a specific recipient
     */
    public void clearEmailsForRecipient(String recipient) {
        receivedEmails.removeIf(email -> email.getTo().equalsIgnoreCase(recipient));
    }

    /**
     * Inner class to represent a mock email
     */
    @Getter
    @AllArgsConstructor
    public static class MockEmail {
        private final String from;
        private final String to;
        private final String subject;
        private final String content;
        private final long timestamp = System.currentTimeMillis();

        @Override
        public String toString() {
            return String.format("MockEmail{from='%s', to='%s', subject='%s', contentLength=%d, timestamp=%d}",
                    from, to, subject, content != null ? content.length() : 0, timestamp);
        }
    }
}
