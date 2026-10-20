-- AyosBarangay schema (MySQL 8.0.16+ style; CHECK constraints are enforced from 8.0.16).
-- Create tables in this order so every foreign key points at a table that already exists.
-- Note: if Spring Boot ever runs this automatically, it needs a datasource configured.
-- Until then, run it yourself in MySQL Workbench or the mysql command line.

CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,              -- hash only, never the password
    role          ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE reports (
    report_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    description     VARCHAR(2000) NOT NULL,
    status          ENUM('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'UNRESOLVABLE')
                        NOT NULL DEFAULT 'PENDING',
    date_submitted  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    address         VARCHAR(255) NOT NULL,
    -- Coordinates optional: a report may be filed with a text address only.
    latitude        DECIMAL(9, 6) NULL,
    longitude       DECIMAL(9, 6) NULL,
    reviewed_by     INT NULL,
    review_date     DATETIME NULL,
    deleted_at      DATETIME NULL,                    -- soft delete: row stays, this is set
    deletion_reason VARCHAR(500) NULL,
    CONSTRAINT fk_reports_user     FOREIGN KEY (user_id)     REFERENCES users (user_id),
    CONSTRAINT fk_reports_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (user_id),
    CONSTRAINT chk_reports_lat     CHECK (latitude  IS NULL OR latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_reports_lng     CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_reports_delete  CHECK (deleted_at IS NULL OR deletion_reason IS NOT NULL),
    INDEX idx_reports_user (user_id),
    INDEX idx_reports_status (status),
    INDEX idx_reports_submitted (date_submitted),
    INDEX idx_reports_location (latitude, longitude),
    INDEX idx_reports_deleted (deleted_at)
);

CREATE TABLE report_images (
    image_id    INT AUTO_INCREMENT PRIMARY KEY,
    report_id   INT NOT NULL,
    image_path  VARCHAR(500) NOT NULL,                -- path or URL, not the file itself
    image_type  ENUM('ORIGINAL', 'RESOLUTION') NOT NULL,
    uploaded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_images_report FOREIGN KEY (report_id) REFERENCES reports (report_id),
    INDEX idx_images_report (report_id)
);

CREATE TABLE resolutions (
    resolution_id          INT AUTO_INCREMENT PRIMARY KEY,
    report_id              INT NOT NULL,
    admin_id               INT NOT NULL,
    resolution_description VARCHAR(2000) NULL,
    resolution_date        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    unresolvable_reason    VARCHAR(2000) NULL,
    CONSTRAINT fk_resolutions_report FOREIGN KEY (report_id) REFERENCES reports (report_id),
    CONSTRAINT fk_resolutions_admin  FOREIGN KEY (admin_id)  REFERENCES users (user_id),
    -- Exactly one of the two texts must be filled in (resolved XOR unresolvable).
    CONSTRAINT chk_resolutions_outcome CHECK (
        (resolution_description IS NOT NULL AND unresolvable_reason IS NULL)
     OR (resolution_description IS NULL AND unresolvable_reason IS NOT NULL)),
    INDEX idx_resolutions_report (report_id)
);

CREATE TABLE report_history (
    history_id    INT AUTO_INCREMENT PRIMARY KEY,
    report_id     INT NOT NULL,
    changed_by    INT NOT NULL,
    old_status    ENUM('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'UNRESOLVABLE') NULL,
    new_status    ENUM('PENDING', 'UNDER_REVIEW', 'RESOLVED', 'UNRESOLVABLE') NOT NULL,
    change_reason VARCHAR(500) NULL,
    changed_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_history_report FOREIGN KEY (report_id)  REFERENCES reports (report_id),
    CONSTRAINT fk_history_user   FOREIGN KEY (changed_by) REFERENCES users (user_id),
    INDEX idx_history_report (report_id, changed_at)
);

-- OPTIONAL: remove this table (and AISummary.java) if summaries won't be saved.
CREATE TABLE ai_summaries (
    summary_id           INT AUTO_INCREMENT PRIMARY KEY,
    generated_by         INT NOT NULL,
    summary_content      TEXT NOT NULL,
    report_count         INT NOT NULL,
    summary_period_start DATE NOT NULL,
    summary_period_end   DATE NOT NULL,
    generated_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_summaries_admin  FOREIGN KEY (generated_by) REFERENCES users (user_id),
    CONSTRAINT chk_summaries_count CHECK (report_count >= 0),
    CONSTRAINT chk_summaries_dates CHECK (summary_period_end >= summary_period_start)
);
