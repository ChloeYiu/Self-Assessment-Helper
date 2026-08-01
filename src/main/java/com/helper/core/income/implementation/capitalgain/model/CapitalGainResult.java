package com.helper.core.income.implementation.capitalgain.model;

import com.helper.core.income.artifact.CarryForwardSnapshotArtifact;
import com.helper.core.income.artifact.IncomeArtifact;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class CapitalGainResult {
    private final BigDecimal capitalGainAmount;
    private final List<IncomeArtifact> artifacts;

    public CapitalGainResult(BigDecimal capitalGainAmount) {
        this.capitalGainAmount = Objects.requireNonNull(capitalGainAmount, "capitalGainAmount");
        this.artifacts = List.of();
    }

    public CapitalGainResult(
            BigDecimal capitalGainAmount,
            CarryForwardSnapshot carryForwardSnapshot
    ) {
        this.capitalGainAmount = Objects.requireNonNull(capitalGainAmount, "capitalGainAmount");
        this.artifacts = List.of(new CarryForwardSnapshotArtifact(
                Objects.requireNonNull(carryForwardSnapshot, "carryForwardSnapshot")
        ));
    }

    public BigDecimal getCapitalGainAmount() {
        return capitalGainAmount;
    }

    public List<IncomeArtifact> getArtifacts() {
        return artifacts;
    }
}
