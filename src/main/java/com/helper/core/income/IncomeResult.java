package com.helper.core.income;

import com.helper.core.income.artifact.IncomeArtifact;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Result produced by calculating an income source.
 */
public class IncomeResult {
    private final IncomeType incomeType;
    private final BigDecimal grossIncome;
    private final BigDecimal adjustedIncome;
    private final List<IncomeArtifact> artifacts;

    public IncomeResult(
            IncomeType incomeType,
            BigDecimal grossIncome,
            BigDecimal adjustedIncome,
            List<IncomeArtifact> artifacts
    ) {
        this.incomeType = Objects.requireNonNull(incomeType, "incomeType");
        this.grossIncome = Objects.requireNonNull(grossIncome, "grossIncome");
        this.adjustedIncome = Objects.requireNonNull(adjustedIncome, "adjustedIncome");
        this.artifacts = List.copyOf(Objects.requireNonNull(artifacts, "artifacts"));
    }

    public IncomeType getIncomeType() {
        return incomeType;
    }

    public BigDecimal getGrossIncome() {
        return grossIncome;
    }

    public BigDecimal getAdjustedIncome() {
        return adjustedIncome;
    }

    public List<IncomeArtifact> getArtifacts() {
        return artifacts;
    }
}
