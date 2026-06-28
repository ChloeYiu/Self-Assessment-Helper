package com.helper.core.income.implementation.capitalgain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Models reported managed-trade capital gain.
 */
public class ManagedTradeCapitalGain implements CapitalGain {

    public int taxYear;
    private final BigDecimal capitalGainAmount;

    public ManagedTradeCapitalGain(int taxYear, BigDecimal capitalGainAmount) {
        this.taxYear = taxYear;
        this.capitalGainAmount = Objects.requireNonNull(capitalGainAmount, "capitalGainAmount");
    }

    @Override
    public BigDecimal calculateCapitalGainAmount() {
        return capitalGainAmount;
    }

    @Override
    public int getTaxYear() {
        return taxYear;
    }
}
