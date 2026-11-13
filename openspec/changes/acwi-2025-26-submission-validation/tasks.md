# Tasks

## 1. Prepare the validation run

- [ ] 1.1 Confirm the final 2025/26 SSGA ERI report is available and identify its URI, distribution date, reporting period, currency, and ACWI ISIN; record the evidence in this task file.
- [ ] 1.2 Confirm the local IBKR Activity and Positions Flex queries and token are valid, then agree a rate-safe manual run procedure; verify a known recent read-only request succeeds without `1018`.
- [ ] 1.3 Check whether the current top-level output exposes enough report, holding, trade, FX, and artifact evidence for review; create a separate implementation change if diagnostics are insufficient.

## 2. Run and inspect the 2025/26 calculation

- [ ] 2.1 Run `TaxCalculatorFlow` for 2025/26 with `DIVIDENDS` selected; record the exact command, requested/effective IBKR dates, and all returned issues.
- [ ] 2.2 Verify that the consolidated ACWI opening holding, imported trades, and report-period boundaries match the underlying IBKR statements and ERI report; record any unavailable range or mismatch as review-required.
- [ ] 2.3 Verify the report currency conversion inputs, generated dividend amount, and closing-holding artifact against hand-reviewed source evidence; do not treat a missing prerequisite as a zero result.

## 3. Decide the outcome

- [ ] 3.1 Record the reviewed 2025/26 result as calculated, review-required, or blocked, with the supporting source evidence and outstanding assumptions.
- [ ] 3.2 Create and link targeted follow-up changes for each defect or unresolved policy found during validation; verify the deferred work has an owner before closing this change.
