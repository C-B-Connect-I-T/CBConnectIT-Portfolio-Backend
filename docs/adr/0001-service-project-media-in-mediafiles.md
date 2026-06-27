# Service and Project media in MediaFiles

Service and Project media are modeled as `MediaFiles` records (not URL columns), with exactly one `IMAGE` and (optionally) one `BANNER` per owner enforced by schema and application validation. Project create requires both files, Service create
requires `IMAGE` and allows `BANNER` to be omitted, update may replace either but must preserve a Service/Project `IMAGE`, and media parsing is handled by a generic multipart helper instead of the testimonial-specific helper. No backfill is performed
because release includes a database reset, so historical Service/Project media in existing environments is intentionally discarded.