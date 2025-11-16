CREATE TABLE notifications (
    id VARCHAR(36) PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    message TEXT,
    template_id VARCHAR(36),
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    priority VARCHAR(20) DEFAULT 'NORMAL',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP
);

CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
