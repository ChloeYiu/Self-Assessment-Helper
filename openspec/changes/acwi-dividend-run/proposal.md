# Proposal: ACWI dividend run

## Why

The first supported end-to-end delivery is an IBKR import for SPDR ACWI accumulating-fund ERI, ISIN `IE00B44Z5B48`, producing a GBP dividend result, closing holding state, and a reviewable import report.

## What Changes

- Add the supported ACWI dividend source-to-result path.
- Report only operations that are executable.
- Preserve local credential handling and explicit report/holding windows.
- Refine the ACWI delivery around `SourceAssemblyPipeline`, provider-specific importers, and dividend orchestration. Reuse existing extraction and domain calculation components.
- Plan the class migration described in I007; preserve unresolved contract and coverage decisions under D010.

## Capabilities

### New Capabilities

- `acwi-dividend-processing`: imports and calculates supported ACWI ERI dividends with reviewable results.

### Modified Capabilities

None.

## Impact

Depends on unresolved D001–D006 and D010 where relevant. This is planning only; it does not authorize implementation.

The affected boundaries are the existing `pipeline` classes and their connection to `TaxCalculator`. Capital-gain execution remains owned by [capital-gains-and-multi-year-state](../capital-gains-and-multi-year-state/proposal.md); new providers, savings delivery, and user-facing category selection remain owned by [other-sources-and-product-access](../other-sources-and-product-access/proposal.md).

## Idea and decision index

- **I007 — Proposed:** `SourceAssemblyPipeline` coordinates provider-specific importers, shared support resolution, and reconciliation before category workflows consume shared inputs. Source: pipeline discussion on 2–3 October 2026; the class migration is detailed in [design.md](design.md). Tasks 1.1–1.10 and 2.1–3.3 describe the ACWI portion. This planning revision does not resolve every contract or authorize implementation.
- **D010 — Open:** exact import/result contracts, account and pool ownership context, category selection, and coverage/reconciliation behavior. See [design.md](design.md#blocking-decisions).
- Related requirements: R1 source-based entry, R2 honest capabilities, R3 preparation/calculation separation, R4 input windows, R5 reviewable imports, R6 category results, R7 reusable state, R8 local credentials. These are retained reference IDs; this change does not allocate new meanings to them.
