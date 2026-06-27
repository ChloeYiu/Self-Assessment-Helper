package com.helper.core.income.implementation.savings;

import com.helper.core.config.CurrencyCode;
import com.helper.core.config.TaxYearPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Models foreign savings income that may require FX conversion into GBP.
 */
public class ForeignSaving implements Saving {

    public CurrencyCode currencyCode;
    public String country;
    public int taxYear;
    private final Map<TaxYearPeriod, BigDecimal> monthlyIncome;
    private final Map<TaxYearPeriod, BigDecimal> monthlyRates;
    private BigDecimal yearlyRate;

    public ForeignSaving(int taxYear, CurrencyCode currencyCode) {
        this.taxYear = taxYear;
        this.currencyCode = Objects.requireNonNull(currencyCode, "currencyCode");
        this.monthlyIncome = new EnumMap<>(TaxYearPeriod.class);
        this.monthlyRates = new EnumMap<>(TaxYearPeriod.class);
    }

    @Override
    public BigDecimal calculateSavingAmount() {
        if (monthlyIncome.isEmpty()) {
            return BigDecimal.ZERO;
        }

        boolean canUseMonthlyRates = hasMonthlyRatesForAllIncome();
        boolean canUseYearlyRate = yearlyRate != null;

        if (canUseMonthlyRates && canUseYearlyRate) {
            return calculateSavingAmountWithMonthlyRates()
                .min(calculateSavingAmountWithYearlyRate());
        }

        if (canUseMonthlyRates) {
            return calculateSavingAmountWithMonthlyRates();
        }

        if (canUseYearlyRate) {
            return calculateSavingAmountWithYearlyRate();
        }

        throw new IllegalStateException("monthly rates or yearly rate must be provided");
    }

    private boolean hasMonthlyRatesForAllIncome() {
        return monthlyIncome.keySet().stream()
            .allMatch(monthlyRates::containsKey);
    }

    public ForeignSavingsCalculationMethod getCalculationMethod() {
        if (monthlyIncome.isEmpty()) {
            return ForeignSavingsCalculationMethod.MONTHLY;
        }

        boolean canUseMonthlyRates = hasMonthlyRatesForAllIncome();
        boolean canUseYearlyRate = yearlyRate != null;

        if (canUseMonthlyRates && canUseYearlyRate) {
            BigDecimal savingsWithMonthlyRates = calculateSavingAmountWithMonthlyRates();
            BigDecimal savingsWithYearlyRate = calculateSavingAmountWithYearlyRate();

            return savingsWithMonthlyRates.compareTo(savingsWithYearlyRate) <= 0
                ? ForeignSavingsCalculationMethod.MONTHLY
                : ForeignSavingsCalculationMethod.YEARLY_AVERAGE;
        }

        if (canUseMonthlyRates) {
            return ForeignSavingsCalculationMethod.MONTHLY;
        }

        if (canUseYearlyRate) {
            return ForeignSavingsCalculationMethod.YEARLY_AVERAGE;
        }

        throw new IllegalStateException("monthly rates or yearly rate must be provided");
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

    public void setMonthlyRate(TaxYearPeriod period, BigDecimal rate) {
        monthlyRates.put(
            Objects.requireNonNull(period, "period"),
            Objects.requireNonNull(rate, "rate")
        );
    }

    public void setYearlyRate(BigDecimal yearlyRate) {
        this.yearlyRate = Objects.requireNonNull(yearlyRate, "yearlyRate");
    }

    public BigDecimal calculateSavingAmountWithMonthlyRates() {
        if (monthlyIncome.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGbp = BigDecimal.ZERO;
        for (Map.Entry<TaxYearPeriod, BigDecimal> entry : monthlyIncome.entrySet()) {
            BigDecimal rate = monthlyRates.get(entry.getKey());
            if (rate == null) {
                throw new IllegalStateException("monthly rate must be set for " + entry.getKey());
            }
            totalGbp = totalGbp.add(convertToGbp(entry.getValue(), rate));
        }

        return totalGbp;
    }

    public BigDecimal calculateSavingAmountWithYearlyRate() {
        if (yearlyRate == null) {
            throw new IllegalStateException("yearlyRate must be set before yearly calculation");
        }

        BigDecimal totalIncome = monthlyIncome.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return convertToGbp(totalIncome, yearlyRate);
    }

    protected BigDecimal convertToGbp(BigDecimal amount, BigDecimal rate) {
        return Objects.requireNonNull(amount, "amount")
            .multiply(Objects.requireNonNull(rate, "rate"))
            .setScale(2, RoundingMode.HALF_UP);
    }
}

enum ForeignSavingsCalculationMethod {
    MONTHLY,
    YEARLY_AVERAGE
}
