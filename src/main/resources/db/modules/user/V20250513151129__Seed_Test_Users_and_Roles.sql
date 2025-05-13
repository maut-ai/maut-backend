
-- Seed Test Users and Roles

-- Password hash for 'password123' is $2a$10$e.VqKYm0b4y8j.0Yc0iS9uK42A/2qY.Q3X1g5LO1p70kXhJzL07u6

DO $$
DECLARE
    admin_user_id UUID := gen_random_uuid();
    client_user_id UUID := gen_random_uuid();
    client_team_id UUID := gen_random_uuid();
    v_role_admin_id UUID;
    v_team_role_owner_id UUID;
BEGIN
    -- Get ROLE_ADMIN id
    SELECT id INTO v_role_admin_id FROM admin_roles WHERE name = 'ROLE_ADMIN' LIMIT 1;

    IF v_role_admin_id IS NULL THEN
        RAISE EXCEPTION 'ROLE_ADMIN not found. Cannot seed test admin user.';
    END IF;

    -- Create ADMIN user
    INSERT INTO users (id, user_type, first_name, last_name, email, password_hash, is_active, created_at, updated_at)
    VALUES (
        admin_user_id,
        'ADMIN',
        'Super',
        'Admin',
        'admin@maut.com',
        '$2a$10$e.VqKYm0b4y8j.0Yc0iS9uK42A/2qY.Q3X1g5LO1p70kXhJzL07u6', -- password123
        TRUE,
        NOW(),
        NOW()
    );

    -- Assign ROLE_ADMIN to ADMIN user
    INSERT INTO user_admin_roles (user_id, admin_role_id)
    VALUES (admin_user_id, v_role_admin_id);

    -- Get ROLE_OWNER id for teams
    SELECT id INTO v_team_role_owner_id FROM team_roles WHERE name = 'ROLE_OWNER' LIMIT 1;

    IF v_team_role_owner_id IS NULL THEN
        RAISE EXCEPTION 'ROLE_OWNER (for teams) not found. Cannot seed test client user and team.';
    END IF;

    -- Create CLIENT user
    INSERT INTO users (id, user_type, first_name, last_name, email, password_hash, is_active, created_at, updated_at)
    VALUES (
        client_user_id,
        'CLIENT',
        'Test',
        'Client',
        'client@maut.com',
        '$2a$10$e.VqKYm0b4y8j.0Yc0iS9uK42A/2qY.Q3X1g5LO1p70kXhJzL07u6', -- password123
        TRUE,
        NOW(),
        NOW()
    );

    -- Create Team for CLIENT user
    INSERT INTO teams (id, name, owner_user_id, created_at, updated_at)
    VALUES (
        client_team_id,
        'Client''s Test Team',
        client_user_id,
        NOW(),
        NOW()
    );

    -- Assign ROLE_OWNER to CLIENT user for their team
    INSERT INTO team_memberships (id, user_id, team_id, team_role_id, joined_at)
    VALUES (
        gen_random_uuid(),
        client_user_id,
        client_team_id,
        v_team_role_owner_id,
        NOW()
    );

END $$;