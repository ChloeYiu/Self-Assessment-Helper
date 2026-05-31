package com.taxcalc.income.implementation.dividend;

import java.math.BigDecimal;

/**
 * Template model for managed-trade dividend income.
 */
public class ManagedTradeDividend implements Dividend {

    public ManagedTradeDividend(int taxYear, String managerName, String accountReference, BigDecimal grossDividend) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal calculateDividendAmount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTaxYear() {
        throw new UnsupportedOperationException();
    }
}
