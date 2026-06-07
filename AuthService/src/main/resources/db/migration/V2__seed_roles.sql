-- V2__seed_roles.sql

INSERT INTO roles (name, description, created_at)
VALUES
    ('ROLE_USER',  'Standard user',          NOW()),
    ('ROLE_ADMIN', 'Administrator',           NOW())
ON DUPLICATE KEY UPDATE name = name;

