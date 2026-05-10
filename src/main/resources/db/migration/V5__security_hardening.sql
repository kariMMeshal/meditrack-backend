-- V5__security_hardening.sql

-- ── Brute force protection columns on users ──────────────────────────
ALTER TABLE users
    ADD COLUMN failed_login_attempts INT          NOT NULL DEFAULT 0,
    ADD COLUMN locked_until          DATETIME     NULL COMMENT 'Account locked until this timestamp; NULL = not locked';

-- ── Refresh tokens ────────────────────────────────────────────────────
CREATE TABLE refresh_tokens (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    token       VARCHAR(64) NOT NULL UNIQUE,
    user_id     INT         NOT NULL,
    expires_at  DATETIME(6) NOT NULL,
    revoked     TINYINT(1)  NOT NULL DEFAULT 0,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_rt_token   ON refresh_tokens(token);
CREATE INDEX idx_rt_user    ON refresh_tokens(user_id);
CREATE INDEX idx_rt_revoked ON refresh_tokens(revoked);

-- ── Audit logs ────────────────────────────────────────────────────────
CREATE TABLE audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    event_type  VARCHAR(50)  NOT NULL COMMENT 'LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, etc.',
    username    VARCHAR(100) NULL,
    ip_address  VARCHAR(45)  NULL,
    user_agent  TEXT         NULL,
    details     TEXT         NULL,
    success     TINYINT(1)   NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_event    ON audit_logs(event_type);
CREATE INDEX idx_audit_created  ON audit_logs(created_at);