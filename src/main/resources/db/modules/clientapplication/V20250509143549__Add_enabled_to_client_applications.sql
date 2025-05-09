ALTER TABLE client_applications
ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN client_applications.enabled IS 'Flag indicating whether the client application is active and can be used. Defaults to TRUE.';
