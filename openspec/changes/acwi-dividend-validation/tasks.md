# Tasks

## 1. Prepare the selected tax-year validation run

- [ ] 1.1 Select the tax year and confirm the final SSGA ERI report is available. Record its URI, distribution date, reporting period, currency, ACWI ISIN, and source provenance.
- [ ] 1.2 Confirm the local IBKR Activity and Positions Flex queries and token are valid. Agree a rate-safe manual run procedure and verify one known recent read-only request succeeds without `1018`.
- [ ] 1.3 Check whether the current top-level output exposes enough report, holding, trade, FX, and artifact evidence for review. Create a separate implementation change if diagnostics are insufficient.
- [ ] 1.4 Decide and record the selected ERI reporting year for the selected tax year. Verify that the report distribution date is within that tax year and the report's period, ISIN, and source are the expected ones. Transferred from `acwi-dividend-run` 1.3.
- [ ] 1.5 Review representative imported IBKR security, trade, and position facts against the statement. Confirm identity, account/provider reference, dates, amounts, source IDs, and the applicable FX policy. Transferred from `acwi-dividend-run` 1.4.

## 2. Run and inspect the calculation

- [ ] 2.1 Run `TaxCalculatorFlow` for the selected tax year with the self-directed investment pipeline selected. Record the exact command, requested/effective IBKR dates, and all returned issues.
- [ ] 2.2 Verify the consolidated ACWI opening holding, imported trades, report-period boundaries, and coverage against the underlying IBKR statements and ERI report. Record an unavailable range, empty statement, zero snapshot, or mismatch as review-required unless the agreed evidence proves it valid. Confirm position/account provenance and the applicable valid-empty versus unavailable-boundary policy. Transferred from `acwi-dividend-run` 1.5, 1.6, 1.6.1, and 3.2.
- [ ] 2.3 Verify the selected method's report input, FX inputs, generated dividend amount, and closing-holding artifact against hand-reviewed source evidence. Exercise and record the agreed invalid report, identity, year, or FX outcome where safely possible; do not treat a missing prerequisite as a zero result. Transferred from `acwi-dividend-run` 1.7 and 3.3.
- [ ] 2.4 Verify the top-level self-directed investment result reaches `IncomeAssembler`, creates the expected dividend income result, and retains its source/preparation issues once. Record the final totals, report/holding/account artifacts, and calculated, ignored, review, or assumption status. Transferred from `acwi-dividend-run` 1.8, 1.9, and 1.10.
- [ ] 2.5 Confirm live Flex `1003` behavior: it produces a local `INFO` unavailable-range issue and adjacent available ranges continue; rate-limit, transport, HTTP, parsing, and other Flex failures stop the run. Record the observed source result and downstream outcome. Transferred from `acwi-dividend-run` 3.4.

## 3. Resolve or carry forward remaining policy work

- [ ] 3.1 Decide whether to replace the local IBKR configuration path with `IbkrFlexConfiguration` in the run `Registry`. If implementation is needed, create and link a separate change; do not add paths or credentials to `TaxCalculationRequest`. Transferred from `acwi-dividend-run` 1.8.2.
- [ ] 3.2 Decide the persistence and reporting policy for holding snapshots, statements, calculated/ignored/review outcomes, and assumptions. If the current output is insufficient, create and link a separate implementation change. Transferred from `acwi-dividend-run` 1.6.1 and 1.9.
- [ ] 3.3 Record the reviewed result as calculated, review-required, or blocked, with supporting evidence and outstanding assumptions. Create and link a targeted follow-up change for every unresolved policy or defect, including duplicate/transfer, account, identity, or incomplete-coverage handling.
