-- =============================================
-- Create MediaFiles table
-- =============================================
CREATE TABLE MediaFiles (
    id                BINARY(16)   NOT NULL PRIMARY KEY,
    url               TEXT         NOT NULL,
    owner_id          BINARY(16)   NOT NULL,
    owner_type        VARCHAR(100) NOT NULL,
    media_type        VARCHAR(100) NOT NULL,
    file_size         BIGINT       NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    alt_text          TEXT         NOT NULL,
    mime_type         VARCHAR(100) NOT NULL,
    width             INT          NULL,
    height            INT          NULL,
    created_at        DATETIME     NOT NULL DEFAULT (datetime('now')),
    updated_at        DATETIME     NOT NULL DEFAULT (datetime('now')),
    CONSTRAINT MediaFiles_owner_unique UNIQUE (owner_id, owner_type)
);

CREATE INDEX idx_MediaFiles_owner_id ON MediaFiles(owner_id);
CREATE INDEX idx_MediaFiles_owner_type ON MediaFiles(owner_type);

-- =============================================
-- Remove image_url column from Testimonials table
-- =============================================
ALTER TABLE Testimonials DROP COLUMN image_url;

-- =============================================
-- Add media file reference to Testimonials table
-- =============================================
ALTER TABLE Testimonials ADD COLUMN avatar_alt_text VARCHAR(255) NOT NULL DEFAULT '';
