package com.helper.core.builder;

import com.helper.core.income.implementation.DividendIncome;
import com.helper.core.income.implementation.dividend.Dividend;

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
