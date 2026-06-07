-- V1__create_user_tables.sql

CREATE TABLE IF NOT EXISTS user_profiles (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_uuid           VARCHAR(36)     NOT NULL,
    username            VARCHAR(50)     NOT NULL,
    display_name        VARCHAR(100)    NOT NULL,
    email               VARCHAR(150)    NULL,
    phone_number        VARCHAR(20)     NULL,
    bio                 VARCHAR(500)    NULL,
    status_message      VARCHAR(150)    NULL,
    profile_photo_url   VARCHAR(500)    NULL,
    is_private          BOOLEAN         NOT NULL DEFAULT FALSE,
    is_active           BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_profiles_uuid     UNIQUE (user_uuid),
    CONSTRAINT uq_user_profiles_username UNIQUE (username)
);

CREATE TABLE IF NOT EXISTS contacts (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    owner_user_uuid     VARCHAR(36)     NOT NULL,
    contact_user_uuid   VARCHAR(36)     NOT NULL,
    contact_name        VARCHAR(100)    NULL,
    is_favorite         BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          DATETIME        NOT NULL,
    updated_at          DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_contacts UNIQUE (owner_user_uuid, contact_user_uuid)
);

CREATE TABLE IF NOT EXISTS blocked_users (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    user_uuid           VARCHAR(36)     NOT NULL,
    blocked_user_uuid   VARCHAR(36)     NOT NULL,
    reason              VARCHAR(255)    NULL,
    created_at          DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_blocked_users UNIQUE (user_uuid, blocked_user_uuid)
);

CREATE INDEX idx_contacts_owner         ON contacts (owner_user_uuid);
CREATE INDEX idx_blocked_users_user     ON blocked_users (user_uuid);
CREATE INDEX idx_user_profiles_username ON user_profiles (username);
