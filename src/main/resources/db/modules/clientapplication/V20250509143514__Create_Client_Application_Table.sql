-- Migration script for creating the client_applications table
CREATE TABLE client_applications (
    id UUID PRIMARY KEY,
    clientName VARCHAR(255) NOT NULL,
    maut_api_client_id VARCHAR(255) NOT NULL UNIQUE,
    clientSecretHash VARCHAR(1024) NOT NULL,
    -- Add other relevant columns for client_applications later as needed
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC')
);

COMMENT ON TABLE client_applications IS 'Stores information about client applications that can interact with the system.';
COMMENT ON COLUMN client_applications.id IS 'Unique identifier for the client application (UUID).';
COMMENT ON COLUMN client_applications.clientName IS 'Human-readable name of the client application.';
COMMENT ON COLUMN client_applications.maut_api_client_id IS 'Maut-issued unique identifier for the client API (public).';
COMMENT ON COLUMN client_applications.clientSecretHash IS 'Hashed client secret for the application.';
COMMENT ON COLUMN client_applications.created_at IS 'Timestamp of when the client application record was created (UTC).';
COMMENT ON COLUMN client_applications.updated_at IS 'Timestamp of when the client application record was last updated (UTC).';
