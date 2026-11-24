package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.income.IncomeType;
import com.helper.income.IncomeWithSource;
import com.helper.income.implementation.savings.Saving;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate for storing multiple savings income sources.
 */
abstract class SavingIncome implements IncomeWithSource<Saving> {

    public BigDecimal grossSavingIncome;
    public TaxYear taxYear;
    private BigDecimal reportedSavingIncome;
    private final List<Saving> savingSources;

    protected SavingIncome(TaxYear taxYear) {
        this.grossSavingIncome = BigDecimal.ZERO;
        this.reportedSavingIncome = BigDecimal.ZERO;
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.savingSources = new ArrayList<>();
    }

    @Override
    public abstract IncomeType getIncomeType();

    @Override
    public IncomeResult calculateResult() {
        BigDecimal calculatedSourceIncome = savingSources.stream()
            .map(Saving::calculateSavingAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        grossSavingIncome = reportedSavingIncome.add(calculatedSourceIncome);
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

    /** Adds an already-GBP amount reported by a provider tax pack. */
    public void addReportedAmount(BigDecimal amountGbp) {
        reportedSavingIncome = reportedSavingIncome.add(Objects.requireNonNull(amountGbp, "amountGbp"));
    }
}
