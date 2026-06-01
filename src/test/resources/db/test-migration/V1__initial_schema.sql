-- =============================================
-- V1: Initial schema for Portfolio Backend
-- Target: SQLite (test environment)
-- Table names match Exposed UUIDTable defaults (PascalCase, "Table" suffix removed)
-- =============================================

CREATE TABLE Tags (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    slug       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT uq_Tags_name UNIQUE (name),
    CONSTRAINT uq_Tags_slug UNIQUE (slug)
);

CREATE TABLE Links (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    url        VARCHAR(255) NOT NULL,
    type       INT          NOT NULL DEFAULT 4,
    created_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME     NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE JobPositions (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME     NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE Companies (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME     NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE Projects (
    id                VARCHAR(36)   NOT NULL PRIMARY KEY,
    banner_image_url  VARCHAR(255) NULL DEFAULT NULL,
    image_url         VARCHAR(255) NULL DEFAULT NULL,
    title             VARCHAR(255)  NOT NULL,
    short_description VARCHAR(1000) NOT NULL,
    description       TEXT          NOT NULL,
    created_at        DATETIME      NOT NULL DEFAULT (datetime('now')),
    updated_at        DATETIME      NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE Users (
    id         VARCHAR(36)  NOT NULL PRIMARY KEY,
    full_name  VARCHAR(100) NULL     DEFAULT NULL,
    username   VARCHAR(100) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at DATETIME     NOT NULL DEFAULT (datetime('now')),
    role       INT          NOT NULL DEFAULT 0, -- UserRoles ordinal: User = 0
    CONSTRAINT uq_Users_username UNIQUE (username)
);

-- Services references Tags (tag_id) and itself (parent_service_id)
CREATE TABLE Services (
    id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
    image_url          VARCHAR(255) NOT NULL,
    banner_image_url   VARCHAR(255) NULL     DEFAULT NULL,
    title              VARCHAR(255) NOT NULL,
    short_description  VARCHAR(1000) NULL     DEFAULT NULL,
    description        TEXT         NOT NULL,
    banner_description TEXT NULL     DEFAULT NULL,
    extra_info         TEXT NULL     DEFAULT NULL,
    tag_id             VARCHAR(36) NULL     DEFAULT NULL,
    parent_service_id  VARCHAR(36) NULL     DEFAULT NULL,
    created_at         DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at         DATETIME     NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT uq_Services_title UNIQUE (title),
    CONSTRAINT fk_Services_tag_id FOREIGN KEY (tag_id) REFERENCES Tags (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_Services_parent_service_id FOREIGN KEY (parent_service_id) REFERENCES Services (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- `from` and `to` are reserved words in MySQL — quoted with backticks
CREATE TABLE Experiences (
    id                VARCHAR(36) NOT NULL PRIMARY KEY,
    job_position_id   VARCHAR(36) NOT NULL,
    short_description TEXT        NOT NULL,
    description       MEDIUMTEXT  NOT NULL,
    as_freelance      TINYINT(1)    NOT NULL DEFAULT 0,
    company_id        VARCHAR(36) NOT NULL,
    `from`            DATETIME    NOT NULL,
    `to`              DATETIME    NOT NULL,
    created_at        DATETIME    NOT NULL DEFAULT (datetime('now')),
    updated_at        DATETIME    NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT fk_Experiences_job_position_id FOREIGN KEY (job_position_id) REFERENCES JobPositions (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_Experiences_company_id FOREIGN KEY (company_id) REFERENCES Companies (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Testimonials (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    image_url       VARCHAR(255) NOT NULL,
    full_name       VARCHAR(255) NOT NULL,
    job_position_id VARCHAR(36)  NOT NULL,
    review          TEXT         NOT NULL,
    company_id      VARCHAR(36) NULL DEFAULT NULL,
    created_at      DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at      DATETIME     NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT fk_Testimonials_job_position_id FOREIGN KEY (job_position_id) REFERENCES JobPositions (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_Testimonials_company_id FOREIGN KEY (company_id) REFERENCES Companies (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Pivot tables (no surrogate PK — uniqueness constraint covers the pair)

CREATE TABLE CompaniesLinksPivot (
    link_id    VARCHAR(36) NOT NULL,
    company_id VARCHAR(36) NOT NULL,
    CONSTRAINT uq_CompaniesLinks UNIQUE (link_id, company_id),
    CONSTRAINT fk_CompaniesLinks_link_id FOREIGN KEY (link_id) REFERENCES Links (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_CompaniesLinks_company_id FOREIGN KEY (company_id) REFERENCES Companies (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE LinksProjectsPivot (
    link_id    VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    CONSTRAINT uq_LinksProjects UNIQUE (project_id, link_id),
    CONSTRAINT fk_LinksProjects_link_id FOREIGN KEY (link_id) REFERENCES Links (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_LinksProjects_project_id FOREIGN KEY (project_id) REFERENCES Projects (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE TagsExperiencesPivot (
    tag_id        VARCHAR(36) NOT NULL,
    experience_id VARCHAR(36) NOT NULL,
    CONSTRAINT uq_TagsExperiences UNIQUE (tag_id, experience_id),
    CONSTRAINT fk_TagsExperiences_tag_id FOREIGN KEY (tag_id) REFERENCES Tags (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_TagsExperiences_experience_id FOREIGN KEY (experience_id) REFERENCES Experiences (id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE TagsProjectsPivot (
    tag_id     VARCHAR(36) NOT NULL,
    project_id VARCHAR(36) NOT NULL,
    CONSTRAINT uq_TagsProjects UNIQUE (project_id, tag_id),
    CONSTRAINT fk_TagsProjects_tag_id FOREIGN KEY (tag_id) REFERENCES Tags (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_TagsProjects_project_id FOREIGN KEY (project_id) REFERENCES Projects (id) ON DELETE CASCADE ON UPDATE CASCADE
);
