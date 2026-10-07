package com.helper.income.implementation.capitalgain;

import com.helper.config.TaxYear;
import com.helper.income.implementation.capitalgain.model.CapitalGainResult;
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
    public CapitalGainResult calculateCapitalGain() {
        return new CapitalGainResult(capitalGainAmount);
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }
}
