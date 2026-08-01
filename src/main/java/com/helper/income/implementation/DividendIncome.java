package com.helper.income.implementation;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.income.IncomeType;
import com.helper.income.IncomeWithSource;
import com.helper.income.implementation.dividend.Dividend;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate for dividend income sources.
 */
public class DividendIncome implements IncomeWithSource<Dividend> {
    private final TaxYear taxYear;
    private final List<Dividend> dividendSources;

    public DividendIncome(TaxYear taxYear) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.dividendSources = new ArrayList<>();
    }

    @Override
    public void addSource(Dividend source) {
        Dividend value = Objects.requireNonNull(source, "source");
        if (!value.getTaxYear().equals(taxYear)) {
            throw new IllegalArgumentException("source tax year must match aggregate tax year");
        }
        dividendSources.add(value);
    }

    @Override
    public IncomeType getIncomeType() {
        return IncomeType.DIVIDENDS;
    }

    @Override
    public IncomeResult calculateResult() {
        BigDecimal grossIncome = dividendSources.stream()
                .map(Dividend::calculateDividendAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new IncomeResult(getIncomeType(), grossIncome, grossIncome, List.of());
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }
}
