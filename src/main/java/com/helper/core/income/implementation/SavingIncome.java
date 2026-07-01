package com.helper.core.income.implementation;

import com.helper.core.config.TaxYear;
import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import com.helper.core.income.implementation.savings.Saving;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate for storing multiple savings income sources.
 */
public class SavingIncome implements Income {

    public BigDecimal grossSavingIncome;
    public TaxYear taxYear;
    private final List<Saving> savingSources;

    public SavingIncome(TaxYear taxYear) {
        this.grossSavingIncome = BigDecimal.ZERO;
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.savingSources = new ArrayList<>();
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.SAVINGS;
    }

    @Override
    public BigDecimal getGrossIncome() {
        if (savingSources.isEmpty()) {
            return grossSavingIncome;
        }

        grossSavingIncome = savingSources.stream()
            .map(Saving::calculateSavingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return grossSavingIncome;
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }

    public void addSavingIncome(Saving savingSource) {
        Saving source = Objects.requireNonNull(savingSource, "savingSource");
        if (!source.getTaxYear().equals(taxYear)) {
            throw new IllegalArgumentException("savingSource tax year must match aggregate tax year");
        }
        savingSources.add(source);
    }
}
