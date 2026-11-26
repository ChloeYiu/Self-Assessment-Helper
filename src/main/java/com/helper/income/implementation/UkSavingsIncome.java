package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeType;

/** Aggregate for savings income reportable in the UK savings category. */
public final class UkSavingsIncome extends SavingIncome {
    public UkSavingsIncome(TaxYear taxYear) {
        super(taxYear);
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.UK_SAVINGS;
    }
}
