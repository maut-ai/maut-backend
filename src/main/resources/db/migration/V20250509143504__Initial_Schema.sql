-- Initial database schema for maut-core-backend
-- This file is for global schema elements if any.
-- Module-specific schemas are managed in their respective db/modules/[module-name] directories.

-- (No global DDL required at this time)

CREATE TABLE maut_users (
    id UUID PRIMARY KEY,
    maut_user_id UUID NOT NULL UNIQUE,
    client_application_id UUID NOT NULL, -- Foreign key constraint will be added in a later migration
    client_system_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT uq_maut_users_client_app_system_id UNIQUE (client_application_id, client_system_user_id)
);

COMMENT ON TABLE maut_users IS 'Stores Maut user core information.';
COMMENT ON COLUMN maut_users.id IS 'Internal DB primary key for the MautUser.';
COMMENT ON COLUMN maut_users.maut_user_id IS 'Maut''s globally unique identifier for the user.';
COMMENT ON COLUMN maut_users.client_application_id IS 'Identifier of the client application this user belongs to (references client_applications.id).';
COMMENT ON COLUMN maut_users.client_system_user_id IS 'The user''s identifier within the client application''s system.';
COMMENT ON COLUMN maut_users.created_at IS 'Timestamp of when the MautUser record was created (UTC).';
COMMENT ON COLUMN maut_users.updated_at IS 'Timestamp of when the MautUser record was last updated (UTC).';
