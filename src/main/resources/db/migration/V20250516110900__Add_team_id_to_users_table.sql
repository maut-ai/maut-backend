ALTER TABLE users
ADD COLUMN team_id UUID;

ALTER TABLE users
ADD CONSTRAINT fk_users_team_id
FOREIGN KEY (team_id)
REFERENCES teams(id)
ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_users_team_id ON users(team_id);
