# Portfolio Backend

Canonical domain language for portfolio content and its media representation.

## Language

**Sub-Service**:
A Service nested under a parent Service. In the public API, sub-services are embedded recursively inside their parent as `sub_services`.
_Avoid_: child service, nested service

**Service Tree**:
The hierarchical representation of Services returned by the public API, where each top-level Service contains its Sub-Services recursively.
_Avoid_: recursive service list, nested services

**Admin Service View**:
A flat, non-hierarchical list of all Services (including Sub-Services) returned exclusively to admins via `GET /services/overview`. Each item carries a compact parent reference instead of embedding children.
_Avoid_: flat service list, service flat list

**Image**:
The primary visual associated with an entity such as a Service or Project.
_Avoid_: imageUrl, primaryImageUrl

**Banner Image**:
A distinct wide/hero visual associated with an entity such as a Service or Project.
_Avoid_: bannerImageUrl

**Banner Removal**:
An explicit maintenance action that removes a Service's Banner Image and its associated Alt Text.
_Avoid_: implicit banner deletion

**Alt Text**:
Human-readable description of an image for accessibility and assistive technologies.
_Avoid_: caption
