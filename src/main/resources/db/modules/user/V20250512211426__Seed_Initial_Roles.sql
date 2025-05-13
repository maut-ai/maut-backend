-- Seed Admin Roles
INSERT INTO admin_roles (id, name, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'ROLE_ADMIN', NOW(), NOW()),
    (gen_random_uuid(), 'ROLE_SUPPORT', NOW(), NOW());

-- Seed Team Roles
INSERT INTO team_roles (id, name, created_at, updated_at)
VALUES
    (gen_random_uuid(), 'ROLE_OWNER', NOW(), NOW()),
    (gen_random_uuid(), 'ROLE_MEMBER', NOW(), NOW()),
    (gen_random_uuid(), 'ROLE_READ_ONLY', NOW(), NOW());