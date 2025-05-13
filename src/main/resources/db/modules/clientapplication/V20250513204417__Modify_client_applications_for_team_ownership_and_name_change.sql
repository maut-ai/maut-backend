-- Modify client_applications table for team ownership and name change

-- Step 1: Rename clientname column to name
ALTER TABLE client_applications RENAME COLUMN clientname TO name;

-- Step 2: Add team_id column
-- Note: If you have existing data, you MUST populate team_id for all existing rows
-- BEFORE applying the NOT NULL constraint in Step 3. For a new table or if you've handled this,
-- you can proceed directly.
ALTER TABLE client_applications ADD COLUMN team_id UUID;

-- Step 3: Add NOT NULL constraint to team_id (after populating existing data if any)
ALTER TABLE client_applications ALTER COLUMN team_id SET NOT NULL;

-- Step 4: Add foreign key constraint for team_id
ALTER TABLE client_applications
ADD CONSTRAINT fk_client_applications_team
FOREIGN KEY (team_id)
REFERENCES teams(id)
ON DELETE RESTRICT;

-- Step 5: Create an index on the team_id column
CREATE INDEX idx_client_applications_team_id ON client_applications(team_id);