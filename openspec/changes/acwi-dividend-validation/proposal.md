# Proposal: ACWI dividend validation

## Why

The current investment pipeline can run a historical empty period, but the account had no activity in 2024/25. A real ACWI dividend calculation can only be validated for a chosen tax year once its final IBKR activity, ERI report, and HMRC FX data are available.

## What Changes

- Run the existing investment/dividend path against the final source data for a selected tax year.
- Verify the chosen SSGA ERI report, opening holding, report-period trades, FX inputs, result, artifacts, and issues.
- Record evidence needed to decide whether the generated dividend figure is suitable for the tax return.
- Create separate follow-up changes for defects or policy decisions discovered during validation; this change does not silently resolve them.

## Capabilities

### New Capabilities

None. This change validates existing ACWI dividend behavior and does not add a product behavior.

### Modified Capabilities

None.

## Impact

This is a future read-only validation exercise over the existing `SelfDirectedInvestmentPipeline`, IBKR Flex configuration, SSGA ERI report source, and HMRC FX data. It owns the remaining real-run verification and policy work transferred from `acwi-dividend-run` tasks 1, 1.3–1.10, and 3.2–3.4. It does not add product behaviour by itself.
