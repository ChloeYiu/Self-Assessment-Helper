package com.helper.pipeline.investment;

import com.helper.income.implementation.dividend.Dividend;
import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipelineResult;
import java.util.List;
import java.util.Objects;

/** Prepared investment sources and non-fatal issues for one tax calculation. */
public record InvestmentPipelineResult(List<Dividend> dividendSources, List<Issue> issues)
        implements TaxPipelineResult {
    public InvestmentPipelineResult {
        dividendSources = List.copyOf(Objects.requireNonNull(dividendSources, "dividendSources"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
