package com.helper.core.builder;

import com.helper.core.income.implementation.SavingIncome;
import com.helper.core.income.implementation.savings.Saving;
import java.util.List;

/**
 * Builder for assembling a SavingIncome object from various sources.
 */
public class SavingIncomeBuilder implements IncomeBuilder<SavingIncome, Saving> {
    public SavingIncomeBuilder() {
        // pass
    }

    public SavingIncome build(int taxYear, List<Saving> sources) {
        throw new UnsupportedOperationException();
    }
}
