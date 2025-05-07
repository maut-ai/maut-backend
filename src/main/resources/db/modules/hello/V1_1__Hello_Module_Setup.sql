-- Hello Module Database Setup
-- Version: 1.1
-- Author: Maut Backend Team

-- Table for storing hello messages
CREATE TABLE hello_message (
    id BIGSERIAL PRIMARY KEY,
    message VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Initial default message
INSERT INTO hello_message (message, updated_at) 
VALUES ('Welcome to the Maut Core Backend!', CURRENT_TIMESTAMP);

-- Add indexes
CREATE INDEX idx_hello_message_updated_at ON hello_message(updated_at);
