package com.helper.core.income.implementation.capitalgain;

import com.helper.core.config.TaxYear;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Models reported managed-trade capital gain.
 */
public class ManagedTradeCapitalGain implements CapitalGain {

    public TaxYear taxYear;
    private final BigDecimal capitalGainAmount;

    public ManagedTradeCapitalGain(TaxYear taxYear, BigDecimal capitalGainAmount) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.capitalGainAmount = Objects.requireNonNull(capitalGainAmount, "capitalGainAmount");
    }

    @Override
    public BigDecimal calculateCapitalGainAmount() {
        return capitalGainAmount;
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }
}
