package com.helper.pipeline.importer;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
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
    private final LocalDate requestedStartDate;
    private final LocalDate requestedEndDate;

    /**
     * Creates imported source facts for a requested provider date range.
     */
    public SourceImportResult(
            List<Security> securities,
            List<Trade> trades,
            LocalDate requestedStartDate,
            LocalDate requestedEndDate
    ) {
        this.securities = List.copyOf(Objects.requireNonNull(securities, "securities"));
        this.trades = List.copyOf(Objects.requireNonNull(trades, "trades"));
        this.requestedStartDate = Objects.requireNonNull(requestedStartDate, "requestedStartDate");
        this.requestedEndDate = Objects.requireNonNull(requestedEndDate, "requestedEndDate");
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

    /** Returns the first date requested from the provider. */
    public LocalDate getRequestedStartDate() {
        return requestedStartDate;
    }

    /** Returns the last date requested from the provider. */
    public LocalDate getRequestedEndDate() {
        return requestedEndDate;
    }
}
