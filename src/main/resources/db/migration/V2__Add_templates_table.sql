-- Migration: V2__Add_templates_table.sql
-- Description: Add notification_templates table and related indexes

-- Create notification_templates table
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

-- Create index on template name for fast lookups
CREATE INDEX IF NOT EXISTS idx_templates_name ON notification_templates(name);

-- Create index on template type for filtering
CREATE INDEX IF NOT EXISTS idx_templates_type ON notification_templates(type);

-- Create index on active templates for quick retrieval
CREATE INDEX IF NOT EXISTS idx_templates_active ON notification_templates(active);

-- Insert default templates
INSERT INTO notification_templates (id, name, type, subject, content, variables, created_by) VALUES
(
    'welcome-email-template',
    'Welcome Email',
    'EMAIL',
    'Welcome to Our Service, {{name}}!',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .header { background: #4CAF50; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; }
        .footer { background: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Welcome to Our Platform!</h1>
    </div>
    <div class="content">
        <h2>Hello {{name}},</h2>
        <p>Thank you for joining our service. We''re excited to have you on board!</p>
        <p>Your account has been successfully created with email: <strong>{{email}}</strong></p>
        <p>Get started by exploring our features and let us know if you need any help.</p>
        <p>Best regards,<br>The Team</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
    </div>
</body>
</html>',
    '["name", "email"]',
    'system'
);
