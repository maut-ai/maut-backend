CREATE TABLE webhook_subscriptions (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL,
    target_url VARCHAR(2048) NOT NULL,
    secret VARCHAR(255) NOT NULL,
    event_types TEXT, -- Stored as a comma-separated string by StringListConverter
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_webhook_subscriptions_team
        FOREIGN KEY (team_id)
        REFERENCES teams(id)
        ON DELETE CASCADE -- If a team is deleted, its webhooks are also deleted.
);

-- Index for faster lookups by team_id
CREATE INDEX idx_webhook_subscriptions_team_id ON webhook_subscriptions(team_id);

-- Ensures that for a given team, an active webhook to a specific target URL is unique.
CREATE UNIQUE INDEX uq_webhook_active_team_target_url
ON webhook_subscriptions (team_id, target_url)
WHERE is_active = TRUE;
