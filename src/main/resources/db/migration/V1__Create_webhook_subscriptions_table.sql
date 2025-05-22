CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY,
    client_application_id UUID NOT NULL,
    target_url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    event_types TEXT, -- Stored as a comma-separated string by StringListConverter
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Index for faster lookups by client_application_id
CREATE INDEX idx_webhook_subscriptions_client_app_id ON webhook_subscriptions(client_application_id);

-- Ensures that for a given client application, an active webhook to a specific target URL is unique.
-- This matches the check performed in WebhookSubscriptionServiceImpl.
CREATE UNIQUE INDEX uq_webhook_active_client_app_target_url 
ON webhook_subscriptions (client_application_id, target_url) 
WHERE is_active = TRUE;

-- If client_application_id is a foreign key to a 'client_applications' table,
-- you would add a constraint like this (ensure 'client_applications' table and its 'id' column exist):
-- ALTER TABLE webhook_subscriptions
-- ADD CONSTRAINT fk_webhook_subscriptions_client_application
-- FOREIGN KEY (client_application_id) REFERENCES client_applications(id);
