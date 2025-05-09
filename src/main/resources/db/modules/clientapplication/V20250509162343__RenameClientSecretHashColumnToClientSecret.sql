-- Renames the client_secret_hash column to client_secret
-- and adjusts the type to VARCHAR(255) for storing plain text secrets.
-- This migration assumes the client_applications table is empty or
-- that loss of existing hashed data is acceptable, as hashes cannot be
-- converted back to plain secrets.

ALTER TABLE client_applications
RENAME COLUMN clientsecrethash TO client_secret;

ALTER TABLE client_applications
ALTER COLUMN client_secret TYPE VARCHAR(255);

-- The entity ClientApplication.java has @Column(name = "client_secret", nullable = false, length = 255)
-- Ensure the NOT NULL constraint is explicitly set in the database schema.
ALTER TABLE client_applications
ALTER COLUMN client_secret SET NOT NULL;