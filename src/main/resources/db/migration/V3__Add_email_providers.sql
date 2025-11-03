-- Migration: V3__Add_email_providers.sql
-- Description: Add email_providers table for multiple SMTP provider support with failover

-- Create email_providers table
CREATE TABLE email_providers (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    host VARCHAR(255) NOT NULL,
    port INTEGER NOT NULL CHECK (port BETWEEN 1 AND 65535),
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    from_email VARCHAR(255) NOT NULL,
    from_name VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    priority INTEGER DEFAULT 1 CHECK (priority >= 1),
    daily_limit INTEGER DEFAULT 1000 CHECK (daily_limit >= 0),
    current_usage INTEGER DEFAULT 0 CHECK (current_usage >= 0),
    max_connection_pool_size INTEGER DEFAULT 5,
    connection_timeout INTEGER DEFAULT 5000,
    timeout INTEGER DEFAULT 5000,
    use_ssl BOOLEAN DEFAULT FALSE,
    use_tls BOOLEAN DEFAULT TRUE,
    last_used TIMESTAMP,
    last_reset TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255)
);

-- Create indexes for efficient querying
CREATE INDEX idx_email_providers_active ON email_providers(active) WHERE active = true;
CREATE INDEX idx_email_providers_priority ON email_providers(priority);
CREATE INDEX idx_email_providers_usage ON email_providers(current_usage);
CREATE INDEX idx_email_providers_last_used ON email_providers(last_used);

-- Create index for finding available providers
CREATE INDEX idx_email_providers_available ON email_providers(active, priority)
WHERE active = true AND current_usage < daily_limit;

-- Add comments to table
COMMENT ON TABLE email_providers IS 'Stores configuration for multiple SMTP providers with failover support';

-- Add comments to columns
COMMENT ON COLUMN email_providers.id IS 'Unique identifier for the provider';
COMMENT ON COLUMN email_providers.name IS 'Unique name for the provider configuration';
COMMENT ON COLUMN email_providers.host IS 'SMTP server hostname';
COMMENT ON COLUMN email_providers.port IS 'SMTP server port';
COMMENT ON COLUMN email_providers.username IS 'SMTP authentication username';
COMMENT ON COLUMN email_providers.password IS 'SMTP authentication password';
COMMENT ON COLUMN email_providers.from_email IS 'Default from email address';
COMMENT ON COLUMN email_providers.from_name IS 'Default from name';
COMMENT ON COLUMN email_providers.active IS 'Whether the provider is active and can be used';
COMMENT ON COLUMN email_providers.priority IS 'Priority for provider selection (lower number = higher priority)';
COMMENT ON COLUMN email_providers.daily_limit IS 'Maximum number of emails allowed per day';
COMMENT ON COLUMN email_providers.current_usage IS 'Number of emails sent today';
COMMENT ON COLUMN email_providers.max_connection_pool_size IS 'Maximum connection pool size for this provider';
COMMENT ON COLUMN email_providers.connection_timeout IS 'Connection timeout in milliseconds';
COMMENT ON COLUMN email_providers.timeout IS 'Socket timeout in milliseconds';
COMMENT ON COLUMN email_providers.use_ssl IS 'Whether to use SSL encryption';
COMMENT ON COLUMN email_providers.use_tls IS 'Whether to use TLS encryption';
COMMENT ON COLUMN email_providers.last_used IS 'Timestamp when provider was last used successfully';
COMMENT ON COLUMN email_providers.last_reset IS 'Timestamp when usage counter was last reset';
COMMENT ON COLUMN email_providers.created_at IS 'Timestamp when provider was created';
COMMENT ON COLUMN email_providers.updated_at IS 'Timestamp when provider was last updated';
COMMENT ON COLUMN email_providers.created_by IS 'User or system that created the provider configuration';

-- Insert default email providers
INSERT INTO email_providers (
    id, name, host, port, username, password, from_email, from_name,
    priority, daily_limit, use_ssl, use_tls, created_by
) VALUES
(
    'smtp-gmail-primary',
    'Gmail Primary',
    'smtp.gmail.com',
    587,
    'your-email@gmail.com',
    'your-app-password',
    'noreply@company.com',
    'Company Notifications',
    1,
    500,
    false,
    true,
    'system'
),
(
    'smtp-gmail-secondary',
    'Gmail Secondary',
    'smtp.gmail.com',
    587,
    'your-backup-email@gmail.com',
    'your-app-password',
    'noreply@company.com',
    'Company Notifications',
    2,
    500,
    false,
    true,
    'system'
),
(
    'smtp-sendgrid',
    'SendGrid',
    'smtp.sendgrid.net',
    587,
    'apikey',
    'your-sendgrid-api-key',
    'noreply@company.com',
    'Company Notifications',
    3,
    10000,
    false,
    true,
    'system'
),
(
    'smtp-mailgun',
    'Mailgun',
    'smtp.mailgun.org',
    587,
    'postmaster@your-domain.mailgun.org',
    'your-mailgun-password',
    'noreply@company.com',
    'Company Notifications',
    4,
    10000,
    false,
    true,
    'system'
),
(
    'smtp-amazon-ses',
    'Amazon SES',
    'email-smtp.us-east-1.amazonaws.com',
    587,
    'your-ses-smtp-username',
    'your-ses-smtp-password',
    'noreply@company.com',
    'Company Notifications',
    5,
    50000,
    false,
    true,
    'system'
);

-- Create provider_usage_log table for tracking provider usage
CREATE TABLE provider_usage_log (
    id VARCHAR(36) PRIMARY KEY,
    provider_id VARCHAR(36) NOT NULL,
    notification_id VARCHAR(36),
    recipient_count INTEGER DEFAULT 1,
    success BOOLEAN NOT NULL,
    error_message TEXT,
    response_time INTEGER, -- in milliseconds
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (provider_id) REFERENCES email_providers(id) ON DELETE CASCADE
);

-- Create indexes for usage log
CREATE INDEX idx_provider_usage_provider_id ON provider_usage_log(provider_id);
CREATE INDEX idx_provider_usage_used_at ON provider_usage_log(used_at);
CREATE INDEX idx_provider_usage_success ON provider_usage_log(success);
CREATE INDEX idx_provider_usage_response_time ON provider_usage_log(response_time);

-- Add comment to usage log table
COMMENT ON TABLE provider_usage_log IS 'Logs usage and performance of email providers';

-- Create provider_health_metrics table for monitoring provider health
CREATE TABLE provider_health_metrics (
    id VARCHAR(36) PRIMARY KEY,
    provider_id VARCHAR(36) NOT NULL,
    check_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_reachable BOOLEAN NOT NULL,
    response_time INTEGER, -- in milliseconds
    error_rate DECIMAL(5,4) DEFAULT 0, -- error rate as decimal (0.0 to 1.0)
    success_rate DECIMAL(5,4) DEFAULT 1.0, -- success rate as decimal (0.0 to 1.0)
    queue_length INTEGER DEFAULT 0,
    FOREIGN KEY (provider_id) REFERENCES email_providers(id) ON DELETE CASCADE
);

-- Create indexes for health metrics
CREATE INDEX idx_provider_health_provider_id ON provider_health_metrics(provider_id);
CREATE INDEX idx_provider_health_timestamp ON provider_health_metrics(check_timestamp);
CREATE INDEX idx_provider_health_reachable ON provider_health_metrics(is_reachable);

-- Add comment to health metrics table
COMMENT ON TABLE provider_health_metrics IS 'Health and performance metrics for email providers';

-- Create function to update provider usage
CREATE OR REPLACE FUNCTION update_provider_usage()
RETURNS TRIGGER AS $$
BEGIN
    -- Update current_usage and last_used when a provider is used
    UPDATE email_providers
    SET
        current_usage = current_usage + NEW.recipient_count,
        last_used = CURRENT_TIMESTAMP
    WHERE id = NEW.provider_id;

    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create trigger to update provider usage
CREATE TRIGGER update_provider_usage_trigger
    AFTER INSERT ON provider_usage_log
    FOR EACH ROW
    WHEN (NEW.success = true)
    EXECUTE FUNCTION update_provider_usage();

-- Create function to reset daily usage counters
CREATE OR REPLACE FUNCTION reset_daily_usage()
RETURNS VOID AS $$
BEGIN
    UPDATE email_providers
    SET
        current_usage = 0,
        last_reset = CURRENT_TIMESTAMP
    WHERE last_reset < CURRENT_DATE;
END;
$$ language 'plpgsql';

-- Create function to get best available provider
CREATE OR REPLACE FUNCTION get_best_available_provider()
RETURNS TABLE(
    provider_id VARCHAR(36),
    provider_name VARCHAR(255),
    host VARCHAR(255),
    port INTEGER,
    username VARCHAR(255),
    password VARCHAR(255),
    from_email VARCHAR(255),
    from_name VARCHAR(255)
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        ep.id,
        ep.name,
        ep.host,
        ep.port,
        ep.username,
        ep.password,
        ep.from_email,
        ep.from_name
    FROM email_providers ep
    WHERE
        ep.active = true
        AND ep.current_usage < ep.daily_limit
    ORDER BY
        ep.priority ASC,
        ep.current_usage::decimal / ep.daily_limit ASC -- Prefer less used providers
    LIMIT 1;
END;
$$ language 'plpgsql';

-- Create function to calculate provider health score
CREATE OR REPLACE FUNCTION calculate_provider_health_score(provider_id VARCHAR)
RETURNS DECIMAL AS $$
DECLARE
    health_score DECIMAL;
    recent_success_rate DECIMAL;
    avg_response_time DECIMAL;
    usage_ratio DECIMAL;
BEGIN
    -- Calculate recent success rate (last hour)
    SELECT
        COALESCE(
            SUM(CASE WHEN success THEN 1 ELSE 0 END)::decimal / NULLIF(COUNT(*), 0),
            1.0
        ) INTO recent_success_rate
    FROM provider_usage_log
    WHERE provider_id = $1
    AND used_at >= (CURRENT_TIMESTAMP - INTERVAL '1 hour');

    -- Calculate average response time (last hour)
    SELECT
        COALESCE(AVG(response_time), 1000) INTO avg_response_time
    FROM provider_usage_log
    WHERE provider_id = $1
    AND success = true
    AND used_at >= (CURRENT_TIMESTAMP - INTERVAL '1 hour');

    -- Calculate usage ratio
    SELECT
        COALESCE(current_usage::decimal / NULLIF(daily_limit, 0), 0) INTO usage_ratio
    FROM email_providers
    WHERE id = $1;

    -- Calculate health score (0.0 to 1.0)
    health_score :=
        (recent_success_rate * 0.5) +
        (GREATEST(0, 1 - (avg_response_time / 5000)) * 0.3) + -- Normalize response time (5s max)
        (GREATEST(0, 1 - usage_ratio) * 0.2); -- Lower score for highly used providers

    RETURN health_score;
END;
$$ language 'plpgsql';

-- Create updated_at trigger for email_providers
CREATE TRIGGER update_email_providers_updated_at
    BEFORE UPDATE ON email_providers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Create view for provider status
CREATE VIEW provider_status AS
SELECT
    ep.id,
    ep.name,
    ep.active,
    ep.priority,
    ep.current_usage,
    ep.daily_limit,
    ep.last_used,
    ROUND((ep.current_usage::decimal / NULLIF(ep.daily_limit, 0)) * 100, 2) as usage_percentage,
    calculate_provider_health_score(ep.id) as health_score,
    CASE
        WHEN ep.active = false THEN 'INACTIVE'
        WHEN ep.current_usage >= ep.daily_limit THEN 'LIMIT_EXCEEDED'
        WHEN calculate_provider_health_score(ep.id) < 0.5 THEN 'UNHEALTHY'
        WHEN ep.last_used IS NULL OR ep.last_used < (CURRENT_TIMESTAMP - INTERVAL '1 hour') THEN 'IDLE'
        ELSE 'HEALTHY'
    END as status
FROM email_providers ep;

-- Add comment to the view
COMMENT ON VIEW provider_status IS 'Current status and health of all email providers';
