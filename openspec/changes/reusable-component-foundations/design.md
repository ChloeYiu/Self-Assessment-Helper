# Design: reusable component foundations

## Context

The 2 October 2026 review found existing tax-year/currency models, tabular and PDF utilities, FX support, a run registry, income calculations, and snapshot/artifact types. Their existence is recorded here; it was not re-verified during the OpenSpec migration.

## Decisions

These components remain reusable foundations. Source-specific ingestion and orchestration stay outside them. No new interfaces or logic are introduced by this change.

## Risks / Trade-offs

Existing domain logic is not proof that a complete source-to-result path works. Delivery changes must still inspect current code and record their own verification evidence.
