package com.helper.pipeline.consolidation;

import com.helper.config.TaxYear;
import com.helper.income.issue.IncomeIssue;
import java.util.List;
import java.util.Objects;

/**
 * Per-instrument source facts consolidated for one tax-year run.
 */
public record SourceConsolidationResult(
        TaxYear taxYear,
        List<ConsolidatedSecurity> securities,
        List<IncomeIssue> issues
) {
    /** Creates an immutable result for the requested tax year. */
    public SourceConsolidationResult {
        taxYear = Objects.requireNonNull(taxYear, "taxYear");
        securities = List.copyOf(Objects.requireNonNull(securities, "securities"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
