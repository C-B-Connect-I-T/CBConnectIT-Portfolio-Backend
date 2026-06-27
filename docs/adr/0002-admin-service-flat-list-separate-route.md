# Admin Service overview lives at /services/overview with a minimal DTO

The public `GET /api/v1/services` returns a recursive Service Tree. Admins need all services in a flat, non-hierarchical list for dashboard maintenance. We added `GET /api/v1/services/overview`, protected by admin authentication, returning a new minimal `ServiceAdminDto` (id, title, compact parent reference, tag, updatedAt).

## Considered Options

- **`?flat=true` query param on existing route** — rejected because it conflates two distinct use cases (public portfolio rendering vs. admin maintenance), complicates auth (this variant must be admin-only), and makes it harder to evolve each shape independently.
- **`/api/v1/admin/services` admin namespace** — rejected because this dashboard feature is services-specific; no other resource currently needs an admin overview, so a top-level admin namespace would imply a growing admin surface that doesn't exist.
- **Return full `ServiceDto` without `sub_services`** — rejected because the admin list requires a `parentService` reference that has no place in the public DTO, and including all media fields in a table row is wasteful.

## Consequences

The admin frontend must call `/api/v1/services/overview` (not the public endpoint) when rendering the maintenance dashboard. The two shapes (`ServiceDto` tree and `ServiceAdminDto` flat) evolve independently.
