
package com.taxcalc.income.implementation.savings;

import com.taxcalc.config.TaxYearPeriod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Map;

/**
 * Models foreign savings income that may require FX conversion into GBP.
 */
public class ForeignSaving implements Saving {

    public String currencyCode;
    public String country;
    public int taxYear;
    private final Map<TaxYearPeriod, BigDecimal> monthlyIncome;
    private final Map<TaxYearPeriod, BigDecimal> monthlyRates;
    private BigDecimal yearlyRate;

    public ForeignSaving(int taxYear, String currencyCode) {
        this.taxYear = taxYear;
        this.currencyCode = currencyCode;
        this.monthlyIncome = new EnumMap<>(TaxYearPeriod.class);
        this.monthlyRates = new EnumMap<>(TaxYearPeriod.class);
        this.yearlyRate = BigDecimal.ZERO;
    }

    @Override
    public BigDecimal calculateSavingAmount() {
        BigDecimal savingsWithMonthlyRates = calculateSavingAmountWithMonthlyRates();
        BigDecimal savingsWithYearlyRate = calculateSavingAmountWithYearlyRate();
        return savingsWithMonthlyRates.max(savingsWithYearlyRate);
    }

    @Override
    public int getTaxYear() {
        return taxYear;
    }

    public ForeignSavingsCalculationMethod getCalculationMethod() {
        BigDecimal savingsWithMonthlyRates = calculateSavingAmountWithMonthlyRates();
        BigDecimal savingsWithYearlyRate = calculateSavingAmountWithYearlyRate();

        return savingsWithMonthlyRates.compareTo(savingsWithYearlyRate) >= 0
            ? ForeignSavingsCalculationMethod.MONTHLY
            : ForeignSavingsCalculationMethod.YEARLY_AVERAGE;
    }

    public void setMonthlyIncome(TaxYearPeriod period, BigDecimal amount) {
        monthlyIncome.put(period, amount);
    }

    public void setMonthlyRate(TaxYearPeriod period, BigDecimal rate) {
        monthlyRates.put(period, rate);
    }

    public void setYearlyRate(BigDecimal yearlyRate) {
        this.yearlyRate = yearlyRate;
    }

    public BigDecimal calculateSavingAmountWithMonthlyRates() {
        if (monthlyIncome.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalGbp = BigDecimal.ZERO;
        for (Map.Entry<TaxYearPeriod, BigDecimal> entry : monthlyIncome.entrySet()) {
            BigDecimal rate = monthlyRates.get(entry.getKey());
            if (rate == null) {
                continue;
            }
            totalGbp = totalGbp.add(convertToGbp(entry.getValue(), rate));
        }

        return totalGbp;
    }

    public BigDecimal calculateSavingAmountWithYearlyRate() {
        if (yearlyRate == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalIncome = monthlyIncome.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return convertToGbp(totalIncome, yearlyRate);
    }

    protected BigDecimal convertToGbp(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}

enum ForeignSavingsCalculationMethod {
    MONTHLY,
    YEARLY_AVERAGE
}

