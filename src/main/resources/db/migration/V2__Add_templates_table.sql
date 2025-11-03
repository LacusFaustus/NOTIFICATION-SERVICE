-- Migration: V2__Add_templates_table.sql
-- Description: Add notification_templates table and related indexes

-- Create notification_templates table
CREATE TABLE notification_templates (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('EMAIL', 'PUSH', 'SMS')),
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
CREATE INDEX idx_templates_name ON notification_templates(name);

-- Create index on template type for filtering
CREATE INDEX idx_templates_type ON notification_templates(type);

-- Create index on active templates for quick retrieval
CREATE INDEX idx_templates_active ON notification_templates(active) WHERE active = true;

-- Create index on updated_at for recently modified templates
CREATE INDEX idx_templates_updated_at ON notification_templates(updated_at);

-- Add comment to table
COMMENT ON TABLE notification_templates IS 'Stores templates for different types of notifications';

-- Add comments to columns
COMMENT ON COLUMN notification_templates.id IS 'Unique identifier for the template';
COMMENT ON COLUMN notification_templates.name IS 'Unique name identifier for the template';
COMMENT ON COLUMN notification_templates.type IS 'Type of notification (EMAIL, PUSH, SMS)';
COMMENT ON COLUMN notification_templates.subject IS 'Subject line for emails or title for push notifications';
COMMENT ON COLUMN notification_templates.content IS 'Template content with placeholders';
COMMENT ON COLUMN notification_templates.variables IS 'JSON array of available template variables';
COMMENT ON COLUMN notification_templates.version IS 'Template version for tracking changes';
COMMENT ON COLUMN notification_templates.active IS 'Whether the template is active and can be used';
COMMENT ON COLUMN notification_templates.created_at IS 'Timestamp when template was created';
COMMENT ON COLUMN notification_templates.updated_at IS 'Timestamp when template was last updated';
COMMENT ON COLUMN notification_templates.created_by IS 'User or system that created the template';

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
),
(
    'password-reset-template',
    'Password Reset',
    'EMAIL',
    'Password Reset Request',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .header { background: #ff6b6b; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; }
        .button { background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }
        .footer { background: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Password Reset</h1>
    </div>
    <div class="content">
        <h2>Hello {{name}},</h2>
        <p>We received a request to reset your password. Click the button below to create a new password:</p>
        <p><a href="{{resetLink}}" class="button">Reset Password</a></p>
        <p>This link will expire in {{expirationHours}} hours.</p>
        <p>If you didn''t request this reset, please ignore this email.</p>
        <p>Best regards,<br>Security Team</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
    </div>
</body>
</html>',
    '["name", "resetLink", "expirationHours"]',
    'system'
),
(
    'order-confirmation-template',
    'Order Confirmation',
    'EMAIL',
    'Order Confirmation - #{{orderNumber}}',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .header { background: #007bff; color: white; padding: 20px; text-align: center; }
        .content { padding: 20px; }
        .order-details { background: #f8f9fa; padding: 15px; border-radius: 5px; }
        .footer { background: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Order Confirmation</h1>
    </div>
    <div class="content">
        <h2>Thank you for your order, {{customerName}}!</h2>
        <div class="order-details">
            <h3>Order Details:</h3>
            <p><strong>Order Number:</strong> {{orderNumber}}</p>
            <p><strong>Order Date:</strong> {{orderDate}}</p>
            <p><strong>Total Amount:</strong> {{totalAmount}}</p>
            <p><strong>Shipping Address:</strong> {{shippingAddress}}</p>
        </div>
        <p>We''ll send you a shipping confirmation when your order is on its way.</p>
        <p>Best regards,<br>Customer Service Team</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
    </div>
</body>
</html>',
    '["customerName", "orderNumber", "orderDate", "totalAmount", "shippingAddress"]',
    'system'
),
(
    'push-notification-template',
    'Generic Push Notification',
    'PUSH',
    '{{title}}',
    '{{message}}',
    '["title", "message", "deepLink"]',
    'system'
),
(
    'system-alert-template',
    'System Alert',
    'EMAIL',
    'System Alert: {{alertType}}',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .header { background: #ffc107; color: #333; padding: 20px; text-align: center; }
        .content { padding: 20px; }
        .alert { background: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; }
        .footer { background: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>System Alert</h1>
    </div>
    <div class="content">
        <div class="alert">
            <h3>Alert Type: {{alertType}}</h3>
            <p><strong>Message:</strong> {{alertMessage}}</p>
            <p><strong>Time:</strong> {{alertTime}}</p>
            <p><strong>Severity:</strong> {{severity}}</p>
        </div>
        <p>Please review and take appropriate action if necessary.</p>
        <p>Best regards,<br>System Monitoring Team</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
    </div>
</body>
</html>',
    '["alertType", "alertMessage", "alertTime", "severity"]',
    'system'
);

-- Create template_audit_log table for tracking template changes
CREATE TABLE template_audit_log (
    id VARCHAR(36) PRIMARY KEY,
    template_id VARCHAR(36) NOT NULL,
    action VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'ACTIVATE', 'DEACTIVATE')),
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (template_id) REFERENCES notification_templates(id) ON DELETE CASCADE
);

-- Create index on template_audit_log for efficient querying
CREATE INDEX idx_template_audit_template_id ON template_audit_log(template_id);
CREATE INDEX idx_template_audit_changed_at ON template_audit_log(changed_at);
CREATE INDEX idx_template_audit_action ON template_audit_log(action);

-- Add comment to audit table
COMMENT ON TABLE template_audit_log IS 'Audit trail for template changes';

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to automatically update updated_at
CREATE TRIGGER update_templates_updated_at
    BEFORE UPDATE ON notification_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create function to log template changes
CREATE OR REPLACE FUNCTION log_template_changes()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'INSERT') THEN
        INSERT INTO template_audit_log (id, template_id, action, new_values, changed_by)
        VALUES (gen_random_uuid()::text, NEW.id, 'CREATE', row_to_json(NEW), NEW.created_by);
        RETURN NEW;
    ELSIF (TG_OP = 'UPDATE') THEN
        INSERT INTO template_audit_log (id, template_id, action, old_values, new_values, changed_by)
        VALUES (gen_random_uuid()::text, NEW.id, 'UPDATE', row_to_json(OLD), row_to_json(NEW), NEW.created_by);
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        INSERT INTO template_audit_log (id, template_id, action, old_values, changed_by)
        VALUES (gen_random_uuid()::text, OLD.id, 'DELETE', row_to_json(OLD), OLD.created_by);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ language 'plpgsql';

-- Create trigger for template audit logging
CREATE TRIGGER audit_template_changes
    AFTER INSERT OR UPDATE OR DELETE ON notification_templates
    FOR EACH ROW
    EXECUTE FUNCTION log_template_changes();
