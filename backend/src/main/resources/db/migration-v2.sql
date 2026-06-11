-- =============================================================================
-- CV Analyzer — schema changes introduced by the enhancement release.
--
-- NOTE: The application runs with spring.jpa.hibernate.ddl-auto=update, so
-- these changes are applied AUTOMATICALLY on startup. This file is provided as
-- a reference / for environments that prefer to apply DDL manually (set
-- ddl-auto=validate after running it). All statements are written to be safe to
-- run against an existing database.
-- =============================================================================

-- ── Feature 1: Google login fields on users ─────────────────────────────────
ALTER TABLE users ADD COLUMN provider       VARCHAR(20)  NULL;
ALTER TABLE users ADD COLUMN google_id       VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN picture         VARCHAR(512) NULL;
ALTER TABLE users ADD COLUMN last_login_at   DATETIME     NULL;
UPDATE users SET provider = 'LOCAL' WHERE provider IS NULL;

-- ── Feature 2: Resume versioning + checksum ─────────────────────────────────
ALTER TABLE resumes ADD COLUMN version_number INT         NULL;
ALTER TABLE resumes ADD COLUMN checksum       VARCHAR(64) NULL;

-- ── Feature 2: Audit log ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NOT NULL,
    user_email  VARCHAR(255) NOT NULL,
    action      VARCHAR(32)  NOT NULL,
    resume_id   BIGINT       NULL,
    ai_provider VARCHAR(64)  NULL,
    details     TEXT         NULL,
    created_at  DATETIME     NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_resume (resume_id)
) ENGINE=InnoDB;

-- ── Feature 7: Generated (tailored) resumes ──────────────────────────────────
CREATE TABLE IF NOT EXISTS generated_resumes (
    id                 BIGINT      NOT NULL AUTO_INCREMENT,
    user_id            BIGINT      NOT NULL,
    original_resume_id BIGINT      NOT NULL,
    version_number     INT         NOT NULL,
    job_description    TEXT        NULL,
    original_content   LONGTEXT    NULL,
    optimized_content  LONGTEXT    NULL,
    change_summary     LONGTEXT    NULL,
    pdf_path           VARCHAR(512) NULL,
    ai_provider        VARCHAR(64) NULL,
    validated          BIT         NOT NULL DEFAULT 0,
    created_at         DATETIME    NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_genres_user   FOREIGN KEY (user_id)            REFERENCES users (id),
    CONSTRAINT fk_genres_resume FOREIGN KEY (original_resume_id) REFERENCES resumes (id)
) ENGINE=InnoDB;
