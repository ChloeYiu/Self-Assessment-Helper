package com.taxcalc.income.implementation;

import com.taxcalc.income.Income;
import com.taxcalc.income.IncomeType;
import com.taxcalc.income.implementation.dividend.Dividend;

import java.math.BigDecimal;

/**
 * Template aggregate for storing multiple dividend income sources.
 */
public class DividendIncome implements Income {

    public DividendIncome(int taxYear) {
        throw new UnsupportedOperationException();
    }

    public void addDividendIncome(Dividend dividendSource) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IncomeType getIncomeType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal getGrossIncome() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getTaxYear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal calculateTaxableAmount() {
        throw new UnsupportedOperationException();
    }
}
