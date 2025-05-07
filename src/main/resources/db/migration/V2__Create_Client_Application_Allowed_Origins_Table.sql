CREATE TABLE client_application_allowed_origins (
    client_application_id UUID NOT NULL,
    origin VARCHAR(255) NOT NULL,
    PRIMARY KEY (client_application_id, origin),
    CONSTRAINT fk_client_application_allowed_origins_client_application
        FOREIGN KEY (client_application_id)
        REFERENCES client_applications (id)
        ON DELETE CASCADE
);

-- Optional: Add an index for faster lookups on origin if needed, though the PK might suffice for typical usage.
-- CREATE INDEX idx_client_app_allowed_origins_origin ON client_application_allowed_origins (origin);
