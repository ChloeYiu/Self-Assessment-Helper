# Proposal: ACWI dividend run

## Why

The first supported end-to-end delivery is an IBKR import for SPDR ACWI accumulating-fund ERI, ISIN `IE00B44Z5B48`, producing a GBP dividend result, closing holding state, and a reviewable import report.

## What Changes

- Add the supported ACWI dividend source-to-result path.
- Report only operations that are executable.
- Preserve local credential handling and explicit report/holding windows.
- Refine the ACWI delivery around `InvestmentAssemblyPipeline`, provider-specific importers, and dividend orchestration. Reuse existing extraction and domain calculation components.
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

- **I007 — Decided for the initial wrapper:** `InvestmentAssemblyPipeline` coordinates one configured `IbkrImporter`, shared support resolution, and consolidation before category workflows consume shared inputs. It derives the shared horizon from the tax input and creates one `IbkrImportInput` with its stored optional configuration path. `IbkrApiProvider` hides the Flex request-size limit by splitting longer ranges into adjacent requests. Source: pipeline discussion on 2–3 and 13 October 2026; the class migration is detailed in [design.md](design.md). Tasks 1.1–1.10 and 2.1–3.3 describe the ACWI portion. A provider-neutral source-selection boundary is deferred until a second provider is onboarded. This decision does not resolve every coverage or ownership policy.
- **D003 — Decided for the initial IBKR path:** `IbkrImporter` makes one Activity Flex request and one Positions request at the shared earliest dividend boundary, 6 October before the tax year. It returns raw, provider-neutral `Position` facts in `SourceImportResult`. When no supported position row is returned, including after Flex `1003`, `SourceConsolidator` uses a zero holding snapshot and retains an `INFO` issue. `SourceConsolidator` filters returned rows to supported ISINs and creates the `SecurityHoldingSnapshot` baseline. `NonUkDomicileAccumulatingDividend` derives each ERI report-end holding by applying later trades to that baseline. The application persists the source statement and accepted snapshot as tax-run artifacts. CG carry-forward state remains separately supplied and persisted. Source: pipeline discussion on 4–5 November 2026; details are in [design.md](design.md#shared-account-trade-horizon-and-cutoff-snapshot).
- **D010 — Open:** exact import/result contracts, account and pool ownership context, category selection, and coverage/reconciliation behavior. See [design.md](design.md#blocking-decisions).
- Related requirements: R1 source-based entry, R2 honest capabilities, R3 preparation/calculation separation, R4 input windows, R5 reviewable imports, R6 category results, R7 reusable state, R8 local credentials. These are retained reference IDs; this change does not allocate new meanings to them.
