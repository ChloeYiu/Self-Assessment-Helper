package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;

/** Aggregate for savings income reportable in the foreign savings category. */
public final class ForeignSavingsIncome extends SavingIncome {
    public ForeignSavingsIncome(TaxYear taxYear) {
        super(taxYear);
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.FOREIGN_SAVINGS;
    }
}
