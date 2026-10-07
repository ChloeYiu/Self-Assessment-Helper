package com.helper.income.implementation.dividend.model;

import com.helper.income.artifact.IncomeArtifact;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * Result of a dividend calculation.
 */
public class DividendResult {
    private final BigDecimal dividendAmount;
    private final List<IncomeArtifact> artifacts;

    /**
     * Creates a dividend result without artifacts.
     */
    public DividendResult(BigDecimal dividendAmount) {
        this(dividendAmount, List.of());
    }

    /**
     * Creates a dividend result with artifacts.
     */
    public DividendResult(BigDecimal dividendAmount, List<IncomeArtifact> artifacts) {
        this.dividendAmount = Objects.requireNonNull(dividendAmount, "dividendAmount");
        this.artifacts = List.copyOf(Objects.requireNonNull(artifacts, "artifacts"));
    }

    public BigDecimal getDividendAmount() {
        return dividendAmount;
    }

    public List<IncomeArtifact> getArtifacts() {
        return artifacts;
    }
}
