-- Create notifications table
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

-- Create notification_templates table
CREATE TABLE notification_templates (
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

-- Create email_providers table
CREATE TABLE email_providers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    from_email VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 1,
    daily_limit INTEGER DEFAULT 1000,
    current_usage INTEGER DEFAULT 0,
    last_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table for push notifications
CREATE TABLE users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    push_token VARCHAR(500),
    platform VARCHAR(50),
    device_id VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create notification_logs table for auditing
CREATE TABLE notification_logs (
    id VARCHAR(36) PRIMARY KEY,
    notification_id VARCHAR(36) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Create indexes
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_recipient ON notifications(recipient);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_templates_name ON notification_templates(name);
CREATE INDEX idx_templates_type ON notification_templates(type);
CREATE INDEX idx_templates_active ON notification_templates(active);
CREATE INDEX idx_email_providers_active ON email_providers(active);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_push_token ON users(push_token);

-- Insert default templates
INSERT INTO notification_templates (id, name, type, subject, content, variables, created_by) VALUES
('welcome-template', 'Welcome Email', 'EMAIL', 'Welcome to Our Service',
'<html><body><h1>Welcome {{name}}!</h1><p>Thank you for joining our service.</p></body></html>',
'["name", "email"]', 'system'),

('password-reset-template', 'Password Reset', 'EMAIL', 'Password Reset Request',
'<html><body><h1>Password Reset</h1><p>Click <a href="{{resetLink}}">here</a> to reset your password.</p></body></html>',
'["resetLink"]', 'system'),

('notification-template', 'General Notification', 'EMAIL', '{{subject}}',
'<html><body><p>{{message}}</p></body></html>',
'["subject", "message"]', 'system');

-- Insert default email providers
INSERT INTO email_providers (id, name, host, port, username, password, from_email, priority) VALUES
('smtp-gmail', 'Gmail SMTP', 'smtp.gmail.com', 587, 'your-email@gmail.com', 'your-app-password', 'noreply@company.com', 1),
('smtp-sendgrid', 'SendGrid', 'smtp.sendgrid.net', 587, 'apikey', 'your-sendgrid-key', 'noreply@company.com', 2);
