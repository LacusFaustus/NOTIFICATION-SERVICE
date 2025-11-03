-- data.sql
-- Initial data population for Notification Service
-- This file contains sample data for development and testing environments

-- Insert sample users for push notifications
INSERT INTO users (id, email, push_token, platform, device_id, active, created_at) VALUES
(
    'user-001',
    'john.doe@example.com',
    'fcm_token_john_android',
    'ANDROID',
    'device_android_001',
    true,
    CURRENT_TIMESTAMP
),
(
    'user-002',
    'jane.smith@example.com',
    'apns_token_jane_ios',
    'IOS',
    'device_ios_001',
    true,
    CURRENT_TIMESTAMP
),
(
    'user-003',
    'bob.wilson@example.com',
    'web_push_token_bob',
    'WEB',
    'device_web_001',
    true,
    CURRENT_TIMESTAMP
),
(
    'user-004',
    'alice.brown@example.com',
    'fcm_token_alice_android',
    'ANDROID',
    'device_android_002',
    true,
    CURRENT_TIMESTAMP
),
(
    'user-005',
    'charlie.davis@example.com',
    NULL,
    'ANDROID',
    'device_android_003',
    false,
    CURRENT_TIMESTAMP
) ON CONFLICT (id) DO NOTHING;

-- Insert additional notification templates
INSERT INTO notification_templates (id, name, type, subject, content, variables, version, active, created_by) VALUES
(
    'weekly-newsletter-template',
    'Weekly Newsletter',
    'EMAIL',
    'Your Weekly Newsletter - {{date}}',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; }
        .content { padding: 30px; background: #f9f9f9; }
        .news-item { background: white; padding: 15px; margin: 15px 0; border-radius: 5px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
        .button { display: inline-block; padding: 12px 24px; background-color: #667eea; color: white; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Weekly Newsletter</h1>
        <p>{{date}}</p>
    </div>
    <div class="content">
        <h2>Hello {{subscriberName}}!</h2>
        <p>Here are this week''s updates:</p>

        <div class="news-item">
            <h3>{{featureTitle1}}</h3>
            <p>{{featureDescription1}}</p>
        </div>

        <div class="news-item">
            <h3>{{featureTitle2}}</h3>
            <p>{{featureDescription2}}</p>
        </div>

        <div style="text-align: center; margin: 30px 0;">
            <a href="{{newsletterLink}}" class="button">Read Full Newsletter</a>
        </div>

        <p>Thank you for being a valued subscriber!</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
        <p><a href="{{unsubscribeLink}}">Unsubscribe</a> from these emails.</p>
    </div>
</body>
</html>',
    '["date", "subscriberName", "featureTitle1", "featureDescription1", "featureTitle2", "featureDescription2", "newsletterLink", "unsubscribeLink"]',
    '1.0',
    true,
    'system'
),
(
    'account-verification-template',
    'Account Verification',
    'EMAIL',
    'Verify Your Account',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
        .header { background: #28a745; color: white; padding: 30px; text-align: center; }
        .content { padding: 30px; background: #f9f9f9; }
        .code { font-size: 32px; font-weight: bold; text-align: center; letter-spacing: 5px; margin: 20px 0; color: #28a745; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Account Verification</h1>
    </div>
    <div class="content">
        <h2>Hello {{name}}!</h2>
        <p>Thank you for creating an account. Please use the verification code below to complete your registration:</p>

        <div class="code">{{verificationCode}}</div>

        <p>This code will expire in {{expirationMinutes}} minutes.</p>
        <p>If you didn''t create an account, please ignore this email.</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
    </div>
</body>
</html>',
    '["name", "verificationCode", "expirationMinutes"]',
    '1.0',
    true,
    'system'
),
(
    'promotional-offer-template',
    'Promotional Offer',
    'EMAIL',
    'Special Offer Just For You!',
    '<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; }
        .header { background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%); color: white; padding: 40px; text-align: center; }
        .content { padding: 30px; background: #f9f9f9; }
        .offer { background: white; padding: 25px; margin: 20px 0; border-radius: 10px; text-align: center; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
        .discount { font-size: 48px; font-weight: bold; color: #ff6b6b; margin: 10px 0; }
        .button { display: inline-block; padding: 15px 30px; background: #ff6b6b; color: white; text-decoration: none; border-radius: 25px; font-size: 18px; margin: 20px 0; }
        .footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="header">
        <h1>Special Offer!</h1>
        <p>Limited Time Promotion</p>
    </div>
    <div class="content">
        <div class="offer">
            <h2>Hello {{customerName}}!</h2>
            <p>As a valued customer, we''re offering you an exclusive discount:</p>
            <div class="discount">{{discountPercentage}}% OFF</div>
            <p>Use code: <strong>{{promoCode}}</strong></p>
            <p>Valid until: {{expiryDate}}</p>
            <a href="{{offerLink}}" class="button">Claim Your Offer</a>
        </div>
        <p>Don''t miss out on this amazing opportunity!</p>
    </div>
    <div class="footer">
        <p>&copy; 2024 Our Company. All rights reserved.</p>
        <p><a href="{{unsubscribeLink}}">Unsubscribe</a> from promotional emails.</p>
    </div>
</body>
</html>',
    '["customerName", "discountPercentage", "promoCode", "expiryDate", "offerLink", "unsubscribeLink"]',
    '1.0',
    true,
    'system'
) ON CONFLICT (id) DO NOTHING;

-- Insert sample notifications for testing
INSERT INTO notifications (id, type, status, recipient, subject, message, template_id, retry_count, priority, created_at, sent_at) VALUES
(
    'notif-001',
    'EMAIL',
    'SENT',
    'john.doe@example.com',
    'Welcome to Our Service, John!',
    'Welcome email content...',
    'welcome-email-template',
    0,
    'NORMAL',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '5 minutes'
),
(
    'notif-002',
    'PUSH',
    'SENT',
    'user-001',
    'New Message Received',
    'You have a new message from Jane',
    'push-notification-template',
    0,
    'HIGH',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP - INTERVAL '1 day' + INTERVAL '10 seconds'
),
(
    'notif-003',
    'EMAIL',
    'FAILED',
    'invalid-email@example',
    'Password Reset Request',
    'Reset password content...',
    'password-reset-template',
    3,
    'NORMAL',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    NULL
),
(
    'notif-004',
    'EMAIL',
    'PENDING',
    'alice.brown@example.com',
    'Weekly Newsletter - 2024-01-15',
    'Newsletter content...',
    'weekly-newsletter-template',
    0,
    'LOW',
    CURRENT_TIMESTAMP - INTERVAL '30 minutes',
    NULL
),
(
    'notif-005',
    'PUSH',
    'SENT',
    'user-002',
    'Order Shipped',
    'Your order #12345 has been shipped',
    'push-notification-template',
    0,
    'NORMAL',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    CURRENT_TIMESTAMP - INTERVAL '2 hours' + INTERVAL '15 seconds'
),
(
    'notif-006',
    'EMAIL',
    'SENT',
    'bob.wilson@example.com',
    'Order Confirmation - #67890',
    'Order confirmation content...',
    'order-confirmation-template',
    0,
    'NORMAL',
    CURRENT_TIMESTAMP - INTERVAL '6 hours',
    CURRENT_TIMESTAMP - INTERVAL '6 hours' + INTERVAL '2 minutes'
) ON CONFLICT (id) DO NOTHING;

-- Insert sample provider usage logs
INSERT INTO provider_usage_log (id, provider_id, notification_id, recipient_count, success, error_message, response_time, used_at) VALUES
(
    'usage-log-001',
    'smtp-gmail-primary',
    'notif-001',
    1,
    true,
    NULL,
    1200,
    CURRENT_TIMESTAMP - INTERVAL '2 days'
),
(
    'usage-log-002',
    'smtp-gmail-primary',
    'notif-003',
    1,
    false,
    'SMTP connection timeout',
    5000,
    CURRENT_TIMESTAMP - INTERVAL '3 hours'
),
(
    'usage-log-003',
    'smtp-gmail-secondary',
    'notif-003',
    1,
    false,
    'Invalid email address',
    800,
    CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
(
    'usage-log-004',
    'smtp-sendgrid',
    'notif-006',
    1,
    true,
    NULL,
    950,
    CURRENT_TIMESTAMP - INTERVAL '6 hours'
) ON CONFLICT (id) DO NOTHING;

-- Insert sample template audit logs
INSERT INTO template_audit_log (id, template_id, action, old_values, new_values, changed_by, changed_at) VALUES
(
    'audit-001',
    'welcome-email-template',
    'UPDATE',
    '{"subject": "Welcome to Our Service", "version": "1.0"}',
    '{"subject": "Welcome to Our Service, {{name}}!", "version": "1.1"}',
    'admin-user',
    CURRENT_TIMESTAMP - INTERVAL '5 days'
),
(
    'audit-002',
    'password-reset-template',
    'ACTIVATE',
    NULL,
    '{"active": true}',
    'system',
    CURRENT_TIMESTAMP - INTERVAL '3 days'
) ON CONFLICT (id) DO NOTHING;

-- Insert sample provider health metrics
INSERT INTO provider_health_metrics (id, provider_id, check_timestamp, is_reachable, response_time, error_rate, success_rate, queue_length) VALUES
(
    'health-001',
    'smtp-gmail-primary',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    true,
    1200,
    0.05,
    0.95,
    0
),
(
    'health-002',
    'smtp-gmail-secondary',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    true,
    1500,
    0.02,
    0.98,
    0
),
(
    'health-003',
    'smtp-sendgrid',
    CURRENT_TIMESTAMP - INTERVAL '1 hour',
    true,
    900,
    0.01,
    0.99,
    0
) ON CONFLICT (id) DO NOTHING;

-- Insert sample notification logs for auditing
INSERT INTO notification_logs (id, notification_id, action, details, created_at, created_by) VALUES
(
    'nlog-001',
    'notif-001',
    'SENT',
    '{"provider": "smtp-gmail-primary", "responseTime": 1200, "recipient": "john.doe@example.com"}',
    CURRENT_TIMESTAMP - INTERVAL '2 days',
    'system'
),
(
    'nlog-002',
    'notif-002',
    'SENT',
    '{"platform": "ANDROID", "deviceId": "device_android_001", "responseTime": 150}',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    'system'
),
(
    'nlog-003',
    'notif-003',
    'FAILED',
    '{"attempt": 1, "error": "SMTP connection timeout", "provider": "smtp-gmail-primary"}',
    CURRENT_TIMESTAMP - INTERVAL '3 hours',
    'system'
),
(
    'nlog-004',
    'notif-003',
    'RETRY',
    '{"attempt": 2, "provider": "smtp-gmail-secondary", "error": "Invalid email address"}',
    CURRENT_TIMESTAMP - INTERVAL '2 hours',
    'system'
) ON CONFLICT (id) DO NOTHING;

-- Update email providers with realistic usage data
UPDATE email_providers
SET
    current_usage = 150,
    last_used = CURRENT_TIMESTAMP - INTERVAL '2 hours'
WHERE id = 'smtp-gmail-primary';

UPDATE email_providers
SET
    current_usage = 75,
    last_used = CURRENT_TIMESTAMP - INTERVAL '4 hours'
WHERE id = 'smtp-gmail-secondary';

UPDATE email_providers
SET
    current_usage = 1200,
    last_used = CURRENT_TIMESTAMP - INTERVAL '1 hour'
WHERE id = 'smtp-sendgrid';

-- Create sample data views for reporting
CREATE OR REPLACE VIEW notification_stats AS
SELECT
    type,
    status,
    COUNT(*) as count,
    AVG(
        CASE
            WHEN sent_at IS NOT NULL AND created_at IS NOT NULL
            THEN EXTRACT(EPOCH FROM (sent_at - created_at))
            ELSE NULL
        END
    ) as avg_processing_time_seconds
FROM notifications
GROUP BY type, status;

CREATE OR REPLACE VIEW provider_performance AS
SELECT
    ep.name as provider_name,
    COUNT(pul.id) as total_attempts,
    SUM(CASE WHEN pul.success THEN 1 ELSE 0 END) as successful_attempts,
    ROUND(
        SUM(CASE WHEN pul.success THEN 1 ELSE 0 END)::decimal /
        NULLIF(COUNT(pul.id), 0) * 100, 2
    ) as success_rate_percentage,
    AVG(pul.response_time) as avg_response_time_ms,
    MAX(pul.used_at) as last_used
FROM email_providers ep
LEFT JOIN provider_usage_log pul ON ep.id = pul.provider_id
GROUP BY ep.id, ep.name
ORDER BY success_rate_percentage DESC NULLS LAST;

-- Print summary of inserted data
DO $$
BEGIN
    RAISE NOTICE 'Data population completed successfully!';
    RAISE NOTICE 'Inserted:';
    RAISE NOTICE '  - % users', (SELECT COUNT(*) FROM users);
    RAISE NOTICE '  - % notification templates', (SELECT COUNT(*) FROM notification_templates);
    RAISE NOTICE '  - % notifications', (SELECT COUNT(*) FROM notifications);
    RAISE NOTICE '  - % email providers', (SELECT COUNT(*) FROM email_providers);
    RAISE NOTICE '  - % provider usage logs', (SELECT COUNT(*) FROM provider_usage_log);
    RAISE NOTICE '  - % template audit logs', (SELECT COUNT(*) FROM template_audit_log);
END $$;
