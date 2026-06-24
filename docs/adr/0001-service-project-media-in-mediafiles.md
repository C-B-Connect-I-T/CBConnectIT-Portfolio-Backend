# Service and Project media in MediaFiles

Service and Project media are modeled as `MediaFiles` records (not URL columns), with exactly one `IMAGE` and one `BANNER` per owner enforced by schema and application validation. Create requires both files, update may replace either but must preserve both, and media parsing is handled by a generic multipart helper instead of the testimonial-specific helper. No backfill is performed because release includes a database reset, so historical Service/Project media in existing environments is intentionally discarded.
