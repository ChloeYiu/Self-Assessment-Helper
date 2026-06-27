package com.taxcalc.builder;

import com.taxcalc.income.implementation.DividendIncome;
import com.taxcalc.income.implementation.dividend.Dividend;

import java.util.List;

/**
 * Builder for assembling a DividendIncome object from various sources.
 */
public class DividendIncomeBuilder implements IncomeBuilder<DividendIncome, Dividend> {

    public DividendIncomeBuilder() {
        // pass
    }

    @Override
    public DividendIncome build(int taxYear, List<Dividend> sources) {
        throw new UnsupportedOperationException();
    }
}
