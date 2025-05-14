-- Add team_id column to maut_users table
ALTER TABLE maut_users
ADD COLUMN team_id UUID;

-- Add foreign key constraint to teams table
ALTER TABLE maut_users
ADD CONSTRAINT fk_maut_users_team_id
FOREIGN KEY (team_id)
REFERENCES teams(id);

-- Add an index on the new team_id column for performance
CREATE INDEX idx_maut_users_team_id ON maut_users(team_id);