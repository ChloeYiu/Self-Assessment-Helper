package com.taxcalc.income.implementation.savings;

import java.math.BigDecimal;

/**
 * Template model for UK local savings income.
 */
public class LocalSaving implements Saving {

    public LocalSaving(int taxYear, String providerName, String accountType, BigDecimal grossInterest) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal calculateSavingAmount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTaxYear() {
        throw new UnsupportedOperationException();
    }
}
