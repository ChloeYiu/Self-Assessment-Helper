package com.helper.core.income.implementation;

import com.helper.core.config.TaxYear;
import com.helper.core.income.IncomeResult;
import com.helper.core.income.IncomeType;
import com.helper.core.income.IncomeWithSource;
import com.helper.core.income.implementation.savings.Saving;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate for storing multiple savings income sources.
 */
public class SavingIncome implements IncomeWithSource<Saving> {

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
    public IncomeResult calculateResult() {
        if (savingSources.isEmpty()) {
            return new IncomeResult(getIncomeType(), grossSavingIncome, grossSavingIncome, List.of());
        }

        grossSavingIncome = savingSources.stream()
            .map(Saving::calculateSavingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new IncomeResult(getIncomeType(), grossSavingIncome, grossSavingIncome, List.of());
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }

    @Override
    public void addSource(Saving source) {
        Saving value = Objects.requireNonNull(source, "source");
        if (!value.getTaxYear().equals(taxYear)) {
            throw new IllegalArgumentException("source tax year must match aggregate tax year");
        }
        savingSources.add(value);
    }
}
