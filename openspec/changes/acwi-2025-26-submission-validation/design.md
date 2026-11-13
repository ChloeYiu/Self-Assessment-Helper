# Design: ACWI 2025/26 submission validation

## Context

See [proposal.md](proposal.md). The present flow has run end to end only for 2024/25, an account period with no available IBKR statements or ACWI holdings. The source and dividend pipeline components exist, but no final 2025/26 result has yet been inspected against the actual report and broker activity.

## Goals / Non-Goals

**Goals:**

- Produce a reproducible, read-only evidence record for the 2025/26 ACWI run.
- Compare the selected ERI report, opening position, imported trades, FX rates, calculated dividend, artifacts, and issues with the underlying sources.
- Classify each outcome as calculated, review-required, or blocked by a fatal retrieval or validation failure.

**Non-Goals:**

- Filing a tax return or submitting data to HMRC.
- Treating an empty or unavailable statement as proof of no historic holdings beyond the documented current policy.
- Resolving transfer, duplicate-import, multi-provider, or incomplete-coverage policy questions without an explicit follow-up change.
- Adding executable tests solely for this manual validation activity.

## Decisions

### D001 — Use the existing top-level flow as the validation entry point

The run will use `TaxCalculatorFlow` for tax year 2025 and select `DIVIDENDS`. It exercises the same application boundary intended for later CLI/UI work and keeps the live query read-only.

**Alternative considered:** invoke import, report extraction, and dividend classes separately. This gives more local diagnostics but does not prove that the integrated result is assembled correctly.

### D002 — Preserve raw-source provenance in the evidence record

The validation record must identify the IBKR requested/effective date range, Flex issues, report URI and reporting period, selected ISIN, opening snapshot date and quantity, matching trades, FX input sources, calculated GBP amount, and generated artifacts. A missing item is a review/blocker outcome, not a zero value.

**Alternative considered:** record only the final dividend amount. That would not make a tax-return figure reviewable.

### D003 — Stop for fatal retrieval or validation errors

IBKR rate-limit, transport, HTTP, parsing, or invalid-report errors prevent a validated result. Recoverable `1003` issues remain visible in the outcome; they require review if they affect required coverage or the opening holding.

**Alternative considered:** retry automatically until a result appears. Repeated Flex requests can trigger rate limits and do not establish missing historical coverage.

## Inputs, outputs, side effects, and error behavior

| Item | Validation design |
| --- | --- |
| Inputs | 2025/26 tax year, local IBKR Flex configuration, fixed ACWI security configuration, live IBKR Activity and Positions statements, SSGA ERI report, and HMRC monthly/yearly FX documents |
| Output | A recorded result containing the GBP dividend figure or a clear review/blocked outcome, with source and artifact evidence |
| Side effects | Read-only network requests to IBKR, SSGA, and HMRC; no filing, account mutation, or persistent run storage in the current implementation |
| Errors | Fatal retrieval/parsing/validation errors stop the run; non-fatal issues remain attached to the result and are reviewed before any tax-return use |

## Risks / Trade-offs

- Live upstream sources may be unavailable or rate-limited → wait and retry only under a documented, rate-safe manual procedure.
- The final ERI report may not yet be published → keep the outcome pending rather than estimating a report value.
- A statement gap may obscure opening holdings → record it as review-required and create a targeted policy/fetch follow-up before relying on the result.
- The current flow prints a compact summary → extend diagnostics only in a separate authorized implementation change if the available artifacts do not support review.

## Migration Plan

1. Start this change when preparing the 2025/26 return and confirm the final ERI report is available.
2. Run the top-level flow under the documented rate-safe procedure.
3. Inspect and record all evidence listed above.
4. Resolve or explicitly defer review items through separate OpenSpec changes.
5. Mark this validation change complete only after the result is reviewable for the return.

## Open Questions

- Does the existing result/artifact output contain enough detail for a reviewer, or will a diagnostics change be needed before the run?
- What evidence is required to accept an unavailable pre-trade Flex range when establishing the ACWI opening holding?
