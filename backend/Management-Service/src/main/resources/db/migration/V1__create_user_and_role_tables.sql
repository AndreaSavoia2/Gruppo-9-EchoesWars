CREATE TABLE roles (
    id INTEGER PRIMARY KEY,
    role VARCHAR(32) NOT NULL,
    role_default BOOLEAN NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_roles_role UNIQUE (role),
    CONSTRAINT ck_roles_role_name
    CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    account_status VARCHAR(32) NOT NULL,
    email_verified_at TIMESTAMP WITH TIME ZONE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_login_at TIMESTAMP WITH TIME ZONE,
    password_changed_at TIMESTAMP WITH TIME ZONE,
    role_id INTEGER NOT NULL,

    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_account_status
    CHECK (
        account_status IN (
          'PENDING_EMAIL_VERIFICATION',
          'ACTIVE',
          'DISABLED',
          'BANNED'
        )
    ),
    CONSTRAINT fk_users_role
    FOREIGN KEY (role_id)
    REFERENCES roles (id)
);

CREATE UNIQUE INDEX ux_users_email_lower
    ON users (LOWER(email));

CREATE INDEX ix_users_role_id
    ON users (role_id);

-- Reference roles used by the management service.
INSERT INTO roles (id, role, role_default)
VALUES
    (1, 'ROLE_USER', TRUE),
    (2, 'ROLE_ADMIN', FALSE);