package com.helper.pipeline.dividend;

import com.helper.income.implementation.dividend.Dividend;
import com.helper.issue.Issue;
import java.util.List;
import java.util.Objects;

/**
 * Combined output from the investment dividend branch for one tax calculation.
 *
 * @param dividendSources prepared dividend sources ready for {@code DividendIncome}
 * @param issues non-fatal conditions from investment dividend preparation
 */
public record DividendMethodResult(
        List<Dividend> dividendSources,
        List<Issue> issues
) {
    /** Creates an immutable investment dividend result. */
    public DividendMethodResult {
        dividendSources = List.copyOf(Objects.requireNonNull(dividendSources, "dividendSources"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
