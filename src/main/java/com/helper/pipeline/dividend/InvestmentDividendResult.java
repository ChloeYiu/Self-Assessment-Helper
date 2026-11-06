package com.helper.pipeline.dividend;

import com.helper.income.implementation.dividend.Dividend;
import com.helper.issue.Issue;
import java.util.List;
import java.util.Objects;

/**
 * The combined dividend sources and non-fatal issues prepared by the investment dividend branch.
 *
 * @param dividendSources dividend sources ready for TaxCalculator to add to DividendIncome
 * @param issues non-fatal issues found while preparing investment dividends
 */
public record InvestmentDividendResult(
        List<Dividend> dividendSources,
        List<Issue> issues
) {
    public InvestmentDividendResult {
        dividendSources = List.copyOf(Objects.requireNonNull(dividendSources, "dividendSources"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
