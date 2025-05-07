CREATE TABLE user_wallets (
    id UUID PRIMARY KEY,
    maut_user_id UUID NOT NULL,
    wallet_address VARCHAR(255) NOT NULL UNIQUE,
    wallet_display_name VARCHAR(255),
    turnkey_sub_organization_id VARCHAR(255) NOT NULL UNIQUE,
    turnkey_maut_private_key_id VARCHAR(255) NOT NULL UNIQUE,
    turnkey_user_private_key_id VARCHAR(255) NOT NULL UNIQUE,
    default_turnkey_policy_id VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    CONSTRAINT fk_user_wallets_maut_user
        FOREIGN KEY (maut_user_id)
        REFERENCES maut_users(id)
        ON DELETE CASCADE
);

COMMENT ON TABLE user_wallets IS 'Stores MPC wallet details associated with Maut users.';
COMMENT ON COLUMN user_wallets.id IS 'Unique identifier for the user wallet record.';
COMMENT ON COLUMN user_wallets.maut_user_id IS 'Foreign key referencing the MautUser to whom this wallet belongs.';
COMMENT ON COLUMN user_wallets.wallet_address IS 'The blockchain address of the wallet (unique).';
COMMENT ON COLUMN user_wallets.wallet_display_name IS 'Optional, user-friendly display name for the wallet.';
COMMENT ON COLUMN user_wallets.turnkey_sub_organization_id IS 'Turnkey sub-organization ID for this wallet (unique).';
COMMENT ON COLUMN user_wallets.turnkey_maut_private_key_id IS 'Turnkey ID for the Maut-held private key share (unique).';
COMMENT ON COLUMN user_wallets.turnkey_user_private_key_id IS 'Turnkey ID for the user-held private key share (unique).';
COMMENT ON COLUMN user_wallets.default_turnkey_policy_id IS 'Optional, ID of the default Turnkey Policy for this wallet.';
COMMENT ON COLUMN user_wallets.created_at IS 'Timestamp of when the wallet record was created (UTC).';
COMMENT ON COLUMN user_wallets.updated_at IS 'Timestamp of when the wallet record was last updated (UTC).';

-- Optional: Index on maut_user_id if frequent lookups by user are expected
-- CREATE INDEX IF NOT EXISTS idx_user_wallets_maut_user_id ON user_wallets(maut_user_id);
