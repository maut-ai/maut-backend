ALTER TABLE user_authenticators
ADD COLUMN last_used_at TIMESTAMP WITH TIME ZONE NULL;

COMMENT ON COLUMN user_authenticators.last_used_at IS 'Timestamp of when the authenticator was last successfully used for authentication.';
