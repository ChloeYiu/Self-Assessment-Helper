package com.taxcalc.calculations;

import com.taxcalc.models.IncomeSource;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calculates total income and deductions from multiple income sources
 */
public class IncomeCalculator {
    
    /**
     * Calculate total gross income from all sources
     */
    public static BigDecimal calculateTotalGrossIncome(List<IncomeSource> incomeSources) {
        return incomeSources.stream()
            .map(IncomeSource::getGrossIncome)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate total allowable deductions from all sources
     */
    public static BigDecimal calculateTotalDeductions(List<IncomeSource> incomeSources) {
        return incomeSources.stream()
            .map(IncomeSource::calculateTaxableAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Calculate net income (gross - deductions)
     */
    public static BigDecimal calculateNetIncome(List<IncomeSource> incomeSources) {
        BigDecimal totalGross = calculateTotalGrossIncome(incomeSources);
        BigDecimal totalDeductions = calculateTotalDeductions(incomeSources);
        return totalGross.subtract(totalDeductions);
    }
    
    /**
     * Calculate taxable income from current income source values.
     * Personal allowance handling is deferred for now.
     */
    public static BigDecimal calculateTaxableIncome(List<IncomeSource> incomeSources) {
        return calculateNetIncome(incomeSources).max(BigDecimal.ZERO);
    }

    /**
     * Backward-compatible overload while TaxAllowance-based logic is paused.
     */
    public static BigDecimal calculateTaxableIncome(List<IncomeSource> incomeSources,
                                                    TaxAllowance taxAllowance) {
        return calculateTaxableIncome(incomeSources);
    }
}
