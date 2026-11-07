package com.helper.pipeline.investment.importer;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.issue.Issue;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Provider-neutral facts imported for one requested date range.
 *
 * <p>The dates identify the range requested from the provider. They do not prove
 * that the provider supplied a complete holdings history for that range.</p>
 */
public class SourceImportResult {
    private final List<Security> securities;
    private final List<Trade> trades;
    private final List<Position> positions;
    private final LocalDate requestedStartDate;
    private final LocalDate requestedEndDate;
    private final List<Issue> issues;

    /** Creates imported source facts, positions, and non-fatal provider issues. */
    public SourceImportResult(
            List<Security> securities,
            List<Trade> trades,
            List<Position> positions,
            LocalDate requestedStartDate,
            LocalDate requestedEndDate,
            List<Issue> issues
    ) {
        this.securities = List.copyOf(Objects.requireNonNull(securities, "securities"));
        this.trades = List.copyOf(Objects.requireNonNull(trades, "trades"));
        this.positions = List.copyOf(Objects.requireNonNull(positions, "positions"));
        this.requestedStartDate = Objects.requireNonNull(requestedStartDate, "requestedStartDate");
        this.requestedEndDate = Objects.requireNonNull(requestedEndDate, "requestedEndDate");
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
        if (requestedEndDate.isBefore(requestedStartDate)) {
            throw new IllegalArgumentException("requestedEndDate must not be before requestedStartDate");
        }
    }

    /** Returns securities detected in the provider source. */
    public List<Security> getSecurities() {
        return securities;
    }

    /** Returns normalized trades detected in the provider source. */
    public List<Trade> getTrades() {
        return trades;
    }

    /** Returns normalized provider-reported positions at requested cutoff dates. */
    public List<Position> getPositions() {
        return positions;
    }

    /** Returns the first date requested from the provider. */
    public LocalDate getRequestedStartDate() {
        return requestedStartDate;
    }

    /** Returns the last date requested from the provider. */
    public LocalDate getRequestedEndDate() {
        return requestedEndDate;
    }

    /** Returns non-fatal conditions encountered while importing this source. */
    public List<Issue> getIssues() {
        return issues;
    }
}
