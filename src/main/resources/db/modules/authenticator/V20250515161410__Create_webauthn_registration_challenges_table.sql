-- Ensures pgcrypto extension is available for gen_random_uuid()
-- CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- This should ideally be in a global/initial migration

CREATE TABLE webauthn_registration_challenges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    maut_user_id UUID NOT NULL REFERENCES maut_users(id) ON DELETE CASCADE,
    challenge TEXT NOT NULL,
    relying_party_id TEXT NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT uq_webauthn_reg_challenge UNIQUE (challenge) -- Challenges should be unique while active
);

CREATE INDEX idx_webauthn_reg_challenges_maut_user_id ON webauthn_registration_challenges(maut_user_id);
CREATE INDEX idx_webauthn_reg_challenges_expires_at ON webauthn_registration_challenges(expires_at);