-- Ensures pgcrypto extension is available for gen_random_uuid()
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- This should ideally be in a global/initial migration

CREATE TABLE maut_user_webauthn_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maut_user_id UUID NOT NULL REFERENCES maut_users(id) ON DELETE CASCADE,
    external_id TEXT NOT NULL, -- Base64URL encoded credential ID from authenticator
    public_key_cose BYTEA NOT NULL, -- COSE-encoded public key
    signature_counter BIGINT NOT NULL,
    transports TEXT[] DEFAULT '{}',
    friendly_name TEXT,
    aaguid TEXT,
    attestation_type TEXT, -- e.g., 'none', 'direct'
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    last_used_at TIMESTAMPTZ,

    CONSTRAINT uq_maut_user_webauthn_external_id UNIQUE (external_id),
    CONSTRAINT uq_maut_user_webauthn_user_external_id UNIQUE (maut_user_id, external_id)
);

CREATE INDEX idx_maut_user_webauthn_maut_user_id ON maut_user_webauthn_credentials(maut_user_id);