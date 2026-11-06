CREATE TABLE authentication_sessions (
                                         id UUID PRIMARY KEY,
                                         user_id UUID NOT NULL,
                                         created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                         last_used_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                         expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                         revoked_at TIMESTAMP WITH TIME ZONE,
                                         revocation_reason VARCHAR(32),
                                         ip_address VARCHAR(45),
                                         user_agent VARCHAR(512),

                                         CONSTRAINT fk_authentication_sessions_user
                                             FOREIGN KEY (user_id)
                                                 REFERENCES users (id)
                                                 ON DELETE RESTRICT,

                                         CONSTRAINT ck_authentication_sessions_expiration
                                             CHECK (expires_at > created_at),

                                         CONSTRAINT ck_authentication_sessions_last_used
                                             CHECK (
                                                 last_used_at >= created_at
                                                     AND last_used_at <= expires_at
                                                 ),

                                         CONSTRAINT ck_authentication_sessions_revocation_metadata
                                             CHECK (
                                                 (revoked_at IS NULL AND revocation_reason IS NULL)
                                                     OR (revoked_at IS NOT NULL AND revocation_reason IS NOT NULL)
                                                 ),

                                         CONSTRAINT ck_authentication_sessions_revocation_reason
                                             CHECK (
                                                 revocation_reason IS NULL
                                                     OR revocation_reason IN (
                                                                              'LOGOUT',
                                                                              'LOGOUT_ALL',
                                                                              'TOKEN_REUSE',
                                                                              'PASSWORD_CHANGE',
                                                                              'ADMIN_ACTION'
                                                     )
                                                 )
);

CREATE INDEX ix_authentication_sessions_user_active
    ON authentication_sessions (user_id, last_used_at DESC)
    WHERE revoked_at IS NULL;

CREATE INDEX ix_authentication_sessions_expiration
    ON authentication_sessions (expires_at);

CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                session_id UUID NOT NULL,
                                token_hash VARCHAR(64) NOT NULL,
                                created_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
                                consumed_at TIMESTAMP WITH TIME ZONE,
                                revoked_at TIMESTAMP WITH TIME ZONE,
                                replaced_by_token_id UUID,

                                CONSTRAINT uq_refresh_tokens_id_session
                                    UNIQUE (id, session_id),

                                CONSTRAINT uq_refresh_tokens_token_hash
                                    UNIQUE (token_hash),

                                CONSTRAINT uq_refresh_tokens_replaced_by_token
                                    UNIQUE (replaced_by_token_id),

                                CONSTRAINT fk_refresh_tokens_session
                                    FOREIGN KEY (session_id)
                                        REFERENCES authentication_sessions (id)
                                        ON DELETE RESTRICT,

                                CONSTRAINT fk_refresh_tokens_replacement
                                    FOREIGN KEY (replaced_by_token_id, session_id)
                                        REFERENCES refresh_tokens (id, session_id)
                                        ON DELETE RESTRICT,

                                CONSTRAINT ck_refresh_tokens_token_hash
                                    CHECK (token_hash ~ '^[0-9a-f]{64}$'),

    CONSTRAINT ck_refresh_tokens_expiration
        CHECK (expires_at > created_at),

    CONSTRAINT ck_refresh_tokens_consumption
        CHECK (
            consumed_at IS NULL
            OR (
                consumed_at >= created_at
                AND consumed_at < expires_at
            )
        ),

    CONSTRAINT ck_refresh_tokens_revocation
        CHECK (
            revoked_at IS NULL
            OR revoked_at >= created_at
        ),

    CONSTRAINT ck_refresh_tokens_replacement_not_self
        CHECK (
            replaced_by_token_id IS NULL
            OR replaced_by_token_id <> id
        ),

    CONSTRAINT ck_refresh_tokens_replacement_requires_consumption
        CHECK (
            replaced_by_token_id IS NULL
            OR consumed_at IS NOT NULL
        )
);

CREATE UNIQUE INDEX ux_refresh_tokens_current_session
    ON refresh_tokens (session_id)
    WHERE consumed_at IS NULL
      AND revoked_at IS NULL;

CREATE INDEX ix_refresh_tokens_session_id
    ON refresh_tokens (session_id);

CREATE INDEX ix_refresh_tokens_expiration
    ON refresh_tokens (expires_at);