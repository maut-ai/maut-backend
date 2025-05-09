-- Add external_authenticator_id and enabled columns to the user_authenticators table

ALTER TABLE user_authenticators
ADD COLUMN external_authenticator_id VARCHAR(255) NOT NULL,
ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

-- Add unique constraint to external_authenticator_id as it should be globally unique
ALTER TABLE user_authenticators
ADD CONSTRAINT UQ_UserAuthenticators_ExternalAuthenticatorId UNIQUE (external_authenticator_id);

-- It's good practice to comment on the new columns
COMMENT ON COLUMN user_authenticators.external_authenticator_id IS 'The external, globally unique ID of the authenticator (e.g., WebAuthn credential ID/rawId).';
COMMENT ON COLUMN user_authenticators.enabled IS 'Indicates whether this authenticator is currently enabled and can be used for authentication.';
