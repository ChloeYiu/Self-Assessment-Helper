package com.helper.core.income.implementation.dividend;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Models reported managed-trade dividend income.
 */
public class ManagedTradeDividend implements Dividend {

    public int taxYear;
    private final BigDecimal dividendAmount;

    public ManagedTradeDividend(int taxYear, BigDecimal dividendAmount) {
        this.taxYear = taxYear;
        this.dividendAmount = Objects.requireNonNull(dividendAmount, "dividendAmount");
    }

    @Override
    public BigDecimal calculateDividendAmount() {
        return dividendAmount;
    }

    @Override
    public int getTaxYear() {
        return taxYear;
    }
}
