
-- Users Table
CREATE TABLE users (
    id UUID PRIMARY KEY,
    user_type VARCHAR(50) NOT NULL, -- ADMIN, CLIENT
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255), -- Nullable initially
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
ALTER TABLE users ADD CONSTRAINT uk_user_email UNIQUE (email);
CREATE INDEX idx_user_email ON users(email);
CREATE INDEX idx_user_type ON users(user_type);

-- Admin Roles Table
CREATE TABLE admin_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
ALTER TABLE admin_roles ADD CONSTRAINT uk_adminrole_name UNIQUE (name);
CREATE INDEX idx_adminrole_name ON admin_roles(name);

-- User Admin Roles Join Table
CREATE TABLE user_admin_roles (
    user_id UUID NOT NULL,
    admin_role_id UUID NOT NULL,
    PRIMARY KEY (user_id, admin_role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (admin_role_id) REFERENCES admin_roles(id) ON DELETE CASCADE
);
CREATE INDEX idx_useradminroles_user_id ON user_admin_roles(user_id);
CREATE INDEX idx_useradminroles_role_id ON user_admin_roles(admin_role_id);

-- Teams Table
CREATE TABLE teams (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_user_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE RESTRICT
);
ALTER TABLE teams ADD CONSTRAINT uk_team_name UNIQUE (name);
CREATE INDEX idx_team_name ON teams(name);
CREATE INDEX idx_team_owner ON teams(owner_user_id);

-- Team Roles Table
CREATE TABLE team_roles (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
ALTER TABLE team_roles ADD CONSTRAINT uk_teamrole_name UNIQUE (name);
CREATE INDEX idx_teamrole_name ON team_roles(name);

-- Team Memberships Table
CREATE TABLE team_memberships (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    team_id UUID NOT NULL,
    team_role_id UUID NOT NULL,
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (team_role_id) REFERENCES team_roles(id) ON DELETE RESTRICT,
    CONSTRAINT uk_user_team UNIQUE (user_id, team_id) -- Matches JPA @UniqueConstraint name
);
CREATE INDEX idx_teammembership_user ON team_memberships(user_id);
CREATE INDEX idx_teammembership_team ON team_memberships(team_id);
CREATE INDEX idx_teammembership_role ON team_memberships(team_role_id);