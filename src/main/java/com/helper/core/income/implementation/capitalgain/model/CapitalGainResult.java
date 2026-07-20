package com.helper.core.income.implementation.capitalgain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class CapitalGainResult {
    private final BigDecimal capitalGainAmount;
    private final CarryForwardSnapshot carryForwardSnapshot;

    public CapitalGainResult(
            BigDecimal capitalGainAmount,
            CarryForwardSnapshot carryForwardSnapshot
    ) {
        this.capitalGainAmount = Objects.requireNonNull(capitalGainAmount, "capitalGainAmount");
        this.carryForwardSnapshot = Objects.requireNonNull(carryForwardSnapshot, "carryForwardSnapshot");
    }

    public BigDecimal getCapitalGainAmount() {
        return capitalGainAmount;
    }

    public CarryForwardSnapshot getCarryForwardSnapshot() {
        return carryForwardSnapshot;
    }

    public List<Match> getMatches() {
        return carryForwardSnapshot.getConsumedAcquisitions();
    }
}
