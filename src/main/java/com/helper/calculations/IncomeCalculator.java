package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.income.Income;
import com.helper.income.IncomeType;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Shell for future income calculation workflow.
 */
public class IncomeCalculator {
    private final TaxYear taxYear;

    public IncomeCalculator(TaxYear taxYear) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
    }

    public void addIncome(Income income) {
        throw new UnsupportedOperationException();
    }

    public BigDecimal calculateAdjustedIncomeByType(IncomeType incomeType) {
        throw new UnsupportedOperationException();
    }

    public TaxYear getTaxYear() {
        return taxYear;
    }
}
