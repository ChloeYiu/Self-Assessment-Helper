package com.helper.core.income.builder;

import com.helper.core.config.TaxYear;
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
    public DividendIncome build(TaxYear taxYear, List<Dividend> sources) {
        throw new UnsupportedOperationException();
    }
}
