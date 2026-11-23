package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;

/** Aggregate for dividend income reportable in the UK dividends category. */
public final class UkDividendIncome extends DividendIncome {
    public UkDividendIncome(TaxYear taxYear) {
        super(taxYear);
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.UK_DIVIDENDS;
    }
}
