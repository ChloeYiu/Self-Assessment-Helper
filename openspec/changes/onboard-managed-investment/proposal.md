# Proposal: onboard managed investment

## Why

The product currently supports self-directed investment facts from IBKR. Managed providers instead supply annual tax packs containing their reported income and capital-gains figures. The product needs a dedicated managed-investment path that can import those packs without pretending they are raw broker trades.

## What Changes

- Add `ManagedInvestmentPipeline`, which owns loading selected managed-provider sources and combines their reported tax facts.
- Onboard J.P. Morgan Personal Investing as the first provider through `JpmpiImporter`.
- Preserve provider/account/tax-year provenance and source locations for reported income and capital-gains evidence.

## Capabilities

### New Capabilities

- `managed-investment-source-import`: ingest provider-produced sources through the managed-investment pipeline.

### Modified Capabilities

None.

## Impact

Bank statements, manual entries, CLI/UI access, exports, storage, and amendments remain outside this change. Future managed providers can be added as provider-specific importers beneath `ManagedInvestmentPipeline`.
