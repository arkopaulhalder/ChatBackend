-- V1__create_auth_tables.sql

CREATE TABLE IF NOT EXISTS auth_users (
    id                          BIGINT          NOT NULL AUTO_INCREMENT,
    user_uuid                   VARCHAR(36)     NOT NULL,
    username                    VARCHAR(50)     NOT NULL,
    email                       VARCHAR(150)    NOT NULL,
    phone_number                VARCHAR(20)     NULL,
    password_hash               VARCHAR(255)    NOT NULL,
    is_enabled                  BOOLEAN         NOT NULL DEFAULT TRUE,
    is_account_non_locked       BOOLEAN         NOT NULL DEFAULT TRUE,
    is_account_non_expired      BOOLEAN         NOT NULL DEFAULT TRUE,
    is_credentials_non_expired  BOOLEAN         NOT NULL DEFAULT TRUE,
    last_login_at               DATETIME        NULL,
    created_at                  DATETIME        NOT NULL,
    updated_at                  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_auth_users_user_uuid  UNIQUE (user_uuid),
    CONSTRAINT uq_auth_users_username   UNIQUE (username),
    CONSTRAINT uq_auth_users_email      UNIQUE (email),
    CONSTRAINT uq_auth_users_phone      UNIQUE (phone_number)
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    name        VARCHAR(50)     NOT NULL,
    description VARCHAR(255)    NULL,
    created_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_roles_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS user_roles (
    id          BIGINT  NOT NULL AUTO_INCREMENT,
    user_id     BIGINT  NOT NULL,
    role_id     BIGINT  NOT NULL,
    created_at  DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_roles UNIQUE (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    user_id     BIGINT          NOT NULL,
    token       VARCHAR(500)    NOT NULL,
    device_id   VARCHAR(100)    NULL,
    device_name VARCHAR(150)    NULL,
    ip_address  VARCHAR(100)    NULL,
    expires_at  DATETIME        NOT NULL,
    revoked     BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  DATETIME        NOT NULL,
    updated_at  DATETIME        NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user  FOREIGN KEY (user_id) REFERENCES auth_users (id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_user_id   ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
