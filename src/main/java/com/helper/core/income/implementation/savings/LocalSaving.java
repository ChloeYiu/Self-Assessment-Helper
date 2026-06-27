package com.helper.core.income.implementation.savings;

import com.helper.core.config.TaxYearPeriod;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Models UK savings income already denominated in GBP.
 */
public class LocalSaving implements Saving {

    public int taxYear;
    private final Map<TaxYearPeriod, BigDecimal> monthlyIncome;

    public LocalSaving(int taxYear) {
        this.taxYear = taxYear;
        this.monthlyIncome = new EnumMap<>(TaxYearPeriod.class);
    }

    @Override
    public BigDecimal calculateSavingAmount() {
        return monthlyIncome.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getTaxYear() {
        return taxYear;
    }

    public void setMonthlyIncome(TaxYearPeriod period, BigDecimal amount) {
        monthlyIncome.put(
            Objects.requireNonNull(period, "period"),
            Objects.requireNonNull(amount, "amount")
        );
    }
}
