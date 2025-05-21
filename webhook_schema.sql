-- Assumes the existence of a 'client_applications' table with a UUID 'id' primary key column.
-- CREATE TABLE client_applications (
--     id UUID PRIMARY KEY,
--     name VARCHAR(255) NOT NULL,
--     -- other relevant fields
--     created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
-- );

CREATE EXTENSION IF NOT EXISTS "uuid-ossp"; -- Required for uuid_generate_v4() on PostgreSQL

CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_application_id UUID NOT NULL,
    target_url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255) NOT NULL, -- Stores the hashed or encrypted secret, or the secret itself if managed externally
    event_types TEXT NOT NULL, -- Stores a comma-separated list or JSON array of event type strings
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_client_application
        FOREIGN KEY(client_application_id)
        REFERENCES client_applications(id)
        ON DELETE CASCADE -- Or ON DELETE RESTRICT based on desired behavior
);

-- Indexes
CREATE INDEX idx_webhook_subscriptions_client_application_id ON webhook_subscriptions(client_application_id);
CREATE INDEX idx_webhook_subscriptions_is_active ON webhook_subscriptions(is_active);

-- Optional: Trigger to automatically update 'updated_at' timestamp on row update (PostgreSQL specific)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = NOW();
   RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_webhook_subscriptions_updated_at
BEFORE UPDATE ON webhook_subscriptions
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

COMMENT ON COLUMN webhook_subscriptions.secret IS 'Stores the client-provided secret for webhook signature verification. Consider encryption at rest or referencing a secret management system.';
COMMENT ON COLUMN webhook_subscriptions.event_types IS 'Stores a comma-separated list or JSON array of subscribed event type strings. e.g., "transaction.succeeded,user.updated" or ["transaction.succeeded", "user.updated"]';
