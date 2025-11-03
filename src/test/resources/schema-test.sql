-- Создание таблиц для тестов
CREATE TABLE IF NOT EXISTS notifications (
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

CREATE TABLE IF NOT EXISTS notification_templates (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    variables TEXT,
    version VARCHAR(20) DEFAULT '1.0',
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);
