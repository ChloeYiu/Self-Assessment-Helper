# Proposal: capital gains and multi-year state

## Why

Existing capital-gain calculation logic needs a source-to-result path and persistent, correctly bounded carry-forward state.

## What Changes

- Import and normalize the capital-gain date window.
- Connect opening pool state and ERI adjustments.
- Return aggregate gain/loss results and closing carry-forward state.

## Capabilities

### New Capabilities

- `capital-gain-processing`: calculate capital gains from normalized taxable transactions and carry-forward state.

### Modified Capabilities

None.

## Impact

Depends on D004, D007, D008, and D010. This is planning only.
