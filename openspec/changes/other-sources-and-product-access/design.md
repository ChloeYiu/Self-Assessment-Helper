# Design: other sources and product access

## Context

Planned sources include HSBC/Cahoot CSV, Citi PDF, JPMPI statements, and manual Rent-a-Room data. Product access may later include a CLI, exports, or a web/API wrapper. Storage remains deferred.

## Decisions

- Source-specific parsing stays separate from generic document mechanics and domain calculation.
- Generic PDF/table utilities do not contain bank-specific tax meaning.
- Storage, amendment history, and snapshot ownership need an agreed design before implementation.

## Open Questions

D008 blocks snapshot storage. Source-specific formats and product-access scope must be selected before this broad change is implemented.
