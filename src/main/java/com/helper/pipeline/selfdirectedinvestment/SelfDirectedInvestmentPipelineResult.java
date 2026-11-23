package com.helper.pipeline.selfdirectedinvestment;

import com.helper.income.implementation.dividend.Dividend;
import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipelineResult;
import java.util.List;
import java.util.Objects;

/** Prepared self-directed investment sources and non-fatal issues for one tax calculation. */
public record SelfDirectedInvestmentPipelineResult(
        List<Dividend> ukDividendSources,
        List<Dividend> foreignDividendSources,
        List<Issue> issues
)
        implements TaxPipelineResult {
    public SelfDirectedInvestmentPipelineResult {
        ukDividendSources = List.copyOf(Objects.requireNonNull(ukDividendSources, "ukDividendSources"));
        foreignDividendSources = List.copyOf(Objects.requireNonNull(
                foreignDividendSources, "foreignDividendSources"));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
