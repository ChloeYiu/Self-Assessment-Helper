package com.helper.pipeline.consolidation;

import com.helper.config.TaxYear;
import java.util.List;
import java.util.Objects;

/**
 * Per-instrument source facts consolidated for one tax-year run.
 */
public record SourceConsolidationResult(
        TaxYear taxYear,
        List<ConsolidatedSecurity> securities
) {
    /** Creates an immutable result for the requested tax year. */
    public SourceConsolidationResult {
        taxYear = Objects.requireNonNull(taxYear, "taxYear");
        securities = List.copyOf(Objects.requireNonNull(securities, "securities"));
    }
}
