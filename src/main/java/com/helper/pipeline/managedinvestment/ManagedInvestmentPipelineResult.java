package com.helper.pipeline.managedinvestment;

import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipelineResult;
import com.helper.income.IncomeType;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Prepared managed-investment sources and recoverable issues for one calculation run.
 */
public record ManagedInvestmentPipelineResult(
        Map<IncomeType, BigDecimal> reportedAmounts,
        List<Issue> issues
) implements TaxPipelineResult {
    public ManagedInvestmentPipelineResult {
        reportedAmounts = Map.copyOf(new EnumMap<>(Objects.requireNonNull(
                reportedAmounts, "reportedAmounts")));
        issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }
}
