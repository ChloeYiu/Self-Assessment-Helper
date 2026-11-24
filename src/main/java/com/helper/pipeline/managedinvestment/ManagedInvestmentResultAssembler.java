package com.helper.pipeline.managedinvestment;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;
import com.helper.issue.Issue;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Consolidates provider-reported managed-investment totals by filing category. */
public final class ManagedInvestmentResultAssembler {
    /**
     * Consolidates provider source results for one tax year into final GBP category amounts.
     *
     * @throws IllegalArgumentException if a source document states a different tax year
     */
    public ManagedInvestmentPipelineResult assemble(
            TaxYear taxYear,
            List<ManagedInvestmentSourceResult> sourceResults
    ) {
        TaxYear calculationTaxYear = Objects.requireNonNull(taxYear, "taxYear");
        Map<IncomeType, BigDecimal> reportedAmounts = new EnumMap<>(IncomeType.class);
        List<Issue> issues = new ArrayList<>();

        for (ManagedInvestmentSourceResult sourceResult : Objects.requireNonNull(
                sourceResults, "sourceResults")) {
            addSourceResult(
                    calculationTaxYear,
                    Objects.requireNonNull(sourceResult, "sourceResult"),
                    reportedAmounts,
                    issues);
        }

        return new ManagedInvestmentPipelineResult(reportedAmounts, issues);
    }

    private static void addSourceResult(
            TaxYear calculationTaxYear,
            ManagedInvestmentSourceResult sourceResult,
            Map<IncomeType, BigDecimal> reportedAmounts,
            List<Issue> issues
    ) {
        if (!calculationTaxYear.equals(sourceResult.taxYear())) {
            throw new IllegalArgumentException("managed-investment source tax year must match calculation tax year");
        }
        sourceResult.reportedAmounts().forEach((incomeType, amount) -> reportedAmounts.merge(
                incomeType, amount, BigDecimal::add));
        issues.addAll(sourceResult.issues());
    }
}
