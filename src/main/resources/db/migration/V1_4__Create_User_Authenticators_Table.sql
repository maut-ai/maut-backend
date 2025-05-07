CREATE TABLE user_authenticators (
    id UUID PRIMARY KEY,
    maut_user_id UUID NOT NULL,
    authenticator_type VARCHAR(255) NOT NULL,
    turnkey_authenticator_id VARCHAR(255) NOT NULL UNIQUE,
    authenticator_name VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT fk_user_authenticators_maut_user
        FOREIGN KEY (maut_user_id)
        REFERENCES maut_users(id)
        ON DELETE CASCADE -- Or RESTRICT/SET NULL depending on desired ownership model
);

COMMENT ON TABLE user_authenticators IS 'Stores authenticator details (e.g., Passkeys) associated with Maut users.';
COMMENT ON COLUMN user_authenticators.id IS 'Unique identifier for the authenticator record.';
COMMENT ON COLUMN user_authenticators.maut_user_id IS 'Foreign key referencing the MautUser to whom this authenticator belongs.';
COMMENT ON COLUMN user_authenticators.authenticator_type IS 'Type of the authenticator (e.g., PASSKEY).';
COMMENT ON COLUMN user_authenticators.turnkey_authenticator_id IS 'Unique identifier for the authenticator from the Turnkey system.';
COMMENT ON COLUMN user_authenticators.authenticator_name IS 'Optional, user-friendly name for the authenticator.';
COMMENT ON COLUMN user_authenticators.created_at IS 'Timestamp of when the authenticator record was created (UTC).';
COMMENT ON COLUMN user_authenticators.updated_at IS 'Timestamp of when the authenticator record was last updated (UTC).';

-- Optional: Index on maut_user_id if frequent lookups by user are expected
-- CREATE INDEX IF NOT EXISTS idx_user_authenticators_maut_user_id ON user_authenticators(maut_user_id);
