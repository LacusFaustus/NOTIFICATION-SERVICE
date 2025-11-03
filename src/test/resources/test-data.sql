-- test-data.sql
-- Test data for unit and integration tests

-- Clean up existing test data
DELETE FROM provider_usage_log;
DELETE FROM template_audit_log;
DELETE FROM notification_logs;
DELETE FROM notifications;
DELETE FROM notification_templates;
DELETE FROM email_providers;
DELETE FROM users;

-- Insert test email providers
INSERT INTO email_providers (id, name, host, port, username, password, from_email, from_name, active, priority, daily_limit, current_usage, created_by) VALUES
('test-provider-1', 'Test SMTP Primary', 'smtp.test.com', 587, 'test-user', 'test-password', 'test@company.com', 'Test Company', true, 1, 1000, 150, 'test-system'),
('test-provider-2', 'Test SMTP Secondary', 'smtp2.test.com', 587, 'test-user2', 'test-password2', 'test@company.com', 'Test Company', true, 2, 500, 50, 'test-system'),
('test-provider-3', 'Test SMTP Inactive', 'smtp3.test.com', 587, 'test-user3', 'test-password3', 'test@company.com', 'Test Company', false, 3, 1000, 0, 'test-system');

-- Insert test notification templates
INSERT INTO notification_templates (id, name, type, subject, content, variables, version, active, created_by) VALUES
('test-template-1', 'Test Welcome Email', 'EMAIL', 'Welcome {{name}}!', '<html><body><h1>Welcome {{name}}!</h1><p>Your email: {{email}}</p></body></html>', '["name", "email"]', '1.0', true, 'test-user'),
('test-template-2', 'Test Password Reset', 'EMAIL', 'Password Reset Request', '<html><body><p>Click <a href="{{resetLink}}">here</a> to reset your password.</p></body></html>', '["resetLink"]', '1.0', true, 'test-user'),
('test-template-3', 'Test Push Notification', 'PUSH', '{{title}}', '{{message}}', '["title", "message"]', '1.0', true, 'test-user'),
('test-template-4', 'Test Inactive Template', 'EMAIL', 'Inactive Template', 'This template is inactive', '[]', '1.0', false, 'test-user');

-- Insert test users
INSERT INTO users (id, email, push_token, platform, device_id, active, preferences, timezone, language, notification_count, last_seen_at) VALUES
('test-user-1', 'john.doe@test.com', 'push-token-123', 'ANDROID', 'device-android-001', true, '{"email_notifications": true, "push_notifications": true}', 'UTC', 'en', 25, CURRENT_TIMESTAMP - INTERVAL '2 days'),
('test-user-2', 'jane.smith@test.com', 'push-token-456', 'IOS', 'device-ios-001', true, '{"email_notifications": true, "push_notifications": false}', 'EST', 'en', 10, CURRENT_TIMESTAMP - INTERVAL '1 day'),
('test-user-3', 'bob.wilson@test.com', NULL, 'WEB', 'device-web-001', true, '{"email_notifications": false, "push_notifications": true}', 'PST', 'en', 5, CURRENT_TIMESTAMP - INTERVAL '3 days'),
('test-user-4', 'inactive.user@test.com', 'push-token-789', 'ANDROID', 'device-android-002', false, '{}', 'UTC', 'en', 0, CURRENT_TIMESTAMP - INTERVAL '30 days');

-- Insert test notifications
INSERT INTO notifications (id, type, status, recipient, subject, message, template_id, retry_count, priority, created_at, sent_at, error_message) VALUES
-- Email notifications
('test-notification-1', 'EMAIL', 'SENT', 'success@test.com', 'Test Email Sent', 'This email was sent successfully', 'test-template-1', 0, 'NORMAL', CURRENT_TIMESTAMP - INTERVAL '2 hours', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL),
('test-notification-2', 'EMAIL', 'FAILED', 'failed@test.com', 'Test Email Failed', 'This email failed to send', 'test-template-1', 3, 'HIGH', CURRENT_TIMESTAMP - INTERVAL '3 hours', NULL, 'SMTP connection timeout'),
('test-notification-3', 'EMAIL', 'PENDING', 'pending@test.com', 'Test Email Pending', 'This email is pending', 'test-template-1', 0, 'NORMAL', CURRENT_TIMESTAMP - INTERVAL '30 minutes', NULL, NULL),
('test-notification-4', 'EMAIL', 'SENT', 'bulk1@test.com', 'Bulk Test 1', 'Bulk email 1', 'test-template-1', 0, 'LOW', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL),
('test-notification-5', 'EMAIL', 'SENT', 'bulk2@test.com', 'Bulk Test 2', 'Bulk email 2', 'test-template-1', 0, 'LOW', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL),

-- Push notifications
('test-notification-6', 'PUSH', 'SENT', 'test-user-1', 'Test Push Sent', 'This push was sent successfully', 'test-template-3', 0, 'HIGH', CURRENT_TIMESTAMP - INTERVAL '1 hour', CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL),
('test-notification-7', 'PUSH', 'FAILED', 'test-user-2', 'Test Push Failed', 'This push failed to send', 'test-template-3', 2, 'NORMAL', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL, 'Invalid push token'),
('test-notification-8', 'PUSH', 'PENDING', 'test-user-1', 'Test Push Pending', 'This push is pending', 'test-template-3', 0, 'NORMAL', CURRENT_TIMESTAMP - INTERVAL '15 minutes', NULL, NULL),

-- Old notifications for testing cleanup and queries
('test-notification-9', 'EMAIL', 'SENT', 'old@test.com', 'Old Notification', 'This is an old notification', 'test-template-1', 0, 'NORMAL', CURRENT_TIMESTAMP - INTERVAL '10 days', CURRENT_TIMESTAMP - INTERVAL '10 days', NULL),
('test-notification-10', 'EMAIL', 'FAILED', 'very.old@test.com', 'Very Old Failed', 'This failed long ago', 'test-template-1', 5, 'HIGH', CURRENT_TIMESTAMP - INTERVAL '20 days', NULL, 'Permanent failure');

-- Insert test provider usage logs
INSERT INTO provider_usage_log (id, provider_id, notification_id, recipient_count, success, error_message, response_time, used_at) VALUES
('test-usage-1', 'test-provider-1', 'test-notification-1', 1, true, NULL, 1200, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('test-usage-2', 'test-provider-1', 'test-notification-2', 1, false, 'SMTP connection timeout', 5000, CURRENT_TIMESTAMP - INTERVAL '3 hours'),
('test-usage-3', 'test-provider-2', 'test-notification-2', 1, false, 'Invalid recipient', 800, CURRENT_TIMESTAMP - INTERVAL '2 hours'),
('test-usage-4', 'test-provider-1', 'test-notification-4', 1, true, NULL, 950, CURRENT_TIMESTAMP - INTERVAL '1 hour'),
('test-usage-5', 'test-provider-1', 'test-notification-5', 1, true, NULL, 1100, CURRENT_TIMESTAMP - INTERVAL '1 hour');

-- Insert test template audit logs
INSERT INTO template_audit_log (id, template_id, action, old_values, new_values, changed_by, changed_at) VALUES
('test-audit-1', 'test-template-1', 'UPDATE', '{"version": "1.0"}', '{"version": "1.1"}', 'test-admin', CURRENT_TIMESTAMP - INTERVAL '5 days'),
('test-audit-2', 'test-template-2', 'CREATE', NULL, '{"name": "Test Password Reset", "active": true}', 'test-system', CURRENT_TIMESTAMP - INTERVAL '3 days'),
('test-audit-3', 'test-template-4', 'DEACTIVATE', '{"active": true}', '{"active": false}', 'test-admin', CURRENT_TIMESTAMP - INTERVAL '1 day');

-- Insert test notification logs
INSERT INTO notification_logs (id, notification_id, action, details, created_at, created_by) VALUES
('test-nlog-1', 'test-notification-1', 'SENT', '{"provider": "test-provider-1", "responseTime": 1200}', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'system'),
('test-nlog-2', 'test-notification-2', 'FAILED', '{"attempt": 1, "error": "SMTP connection timeout", "provider": "test-provider-1"}', CURRENT_TIMESTAMP - INTERVAL '3 hours', 'system'),
('test-nlog-3', 'test-notification-2', 'RETRY', '{"attempt": 2, "provider": "test-provider-2", "error": "Invalid recipient"}', CURRENT_TIMESTAMP - INTERVAL '2 hours', 'system'),
('test-nlog-4', 'test-notification-6', 'SENT', '{"platform": "ANDROID", "deviceId": "device-android-001"}', CURRENT_TIMESTAMP - INTERVAL '1 hour', 'system');

-- Update email provider usage counts to match test data
UPDATE email_providers SET current_usage = 3 WHERE id = 'test-provider-1';
UPDATE email_providers SET current_usage = 1 WHERE id = 'test-provider-2';

-- Print test data summary
DO $$
BEGIN
    RAISE NOTICE 'Test data inserted:';
    RAISE NOTICE '  - % email providers', (SELECT COUNT(*) FROM email_providers);
    RAISE NOTICE '  - % notification templates', (SELECT COUNT(*) FROM notification_templates);
    RAISE NOTICE '  - % users', (SELECT COUNT(*) FROM users);
    RAISE NOTICE '  - % notifications', (SELECT COUNT(*) FROM notifications);
    RAISE NOTICE '  - % provider usage logs', (SELECT COUNT(*) FROM provider_usage_log);
    RAISE NOTICE '  - % template audit logs', (SELECT COUNT(*) FROM template_audit_log);
    RAISE NOTICE '  - % notification logs', (SELECT COUNT(*) FROM notification_logs);
END $$;
