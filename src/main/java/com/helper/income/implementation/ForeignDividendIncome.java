package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;

/** Aggregate for dividend income reportable in the foreign dividends category. */
public final class ForeignDividendIncome extends DividendIncome {
    public ForeignDividendIncome(TaxYear taxYear) {
        super(taxYear);
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.FOREIGN_DIVIDENDS;
    }
}
