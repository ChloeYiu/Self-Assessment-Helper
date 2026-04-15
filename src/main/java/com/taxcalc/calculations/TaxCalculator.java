package com.taxcalc.calculations;

import com.taxcalc.models.IncomeSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Main tax calculation engine
 * Calculates income tax owed based on UK tax brackets and allowances
 */
public class TaxCalculator {
    
    private TaxAllowance taxAllowance;
    
    public TaxCalculator(TaxAllowance taxAllowance) {
        this.taxAllowance = taxAllowance;
    }
    
    /**
     * Calculate total income tax owing
     */
    public BigDecimal calculateIncomeTax(List<IncomeSource> incomeSources) {
        BigDecimal taxableIncome = IncomeCalculator.calculateTaxableIncome(incomeSources, taxAllowance);
        
        if (taxableIncome.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal basicRateLimit = taxAllowance.basicRateBandLimit
            .subtract(taxAllowance.personalAllowance);
        
        // Basic rate tax (20%)
        if (taxableIncome.compareTo(basicRateLimit) <= 0) {
            tax = taxableIncome.multiply(taxAllowance.basicRatePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            // Tax on basic rate portion
            tax = basicRateLimit.multiply(taxAllowance.basicRatePercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            BigDecimal higherRateLimit = taxAllowance.higherRateBandLimit
                .subtract(taxAllowance.personalAllowance);
            
            // Higher rate tax (40%)
            if (taxableIncome.compareTo(higherRateLimit) <= 0) {
                BigDecimal higherRatePortion = taxableIncome.subtract(basicRateLimit);
                tax = tax.add(higherRatePortion.multiply(taxAllowance.higherRatePercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            } else {
                // Higher rate portion
                BigDecimal higherRatePortion = higherRateLimit.subtract(basicRateLimit);
                tax = tax.add(higherRatePortion.multiply(taxAllowance.higherRatePercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                
                // Additional rate (45%)
                BigDecimal additionalRatePortion = taxableIncome.subtract(higherRateLimit);
                tax = tax.add(additionalRatePortion.multiply(taxAllowance.additionalRatePercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
            }
        }
        
        return tax;
    }
    
    /**
     * Get a detailed tax summary
     */
    public TaxSummary calculateTaxSummary(List<IncomeSource> incomeSources) {
        BigDecimal totalGross = IncomeCalculator.calculateTotalGrossIncome(incomeSources);
        BigDecimal totalDeductions = IncomeCalculator.calculateTotalDeductions(incomeSources);
        BigDecimal netIncome = IncomeCalculator.calculateNetIncome(incomeSources);
        BigDecimal taxableIncome = IncomeCalculator.calculateTaxableIncome(incomeSources, taxAllowance);
        BigDecimal taxOwing = calculateIncomeTax(incomeSources);
        
        return new TaxSummary(
            totalGross,
            totalDeductions,
            netIncome,
            taxAllowance.personalAllowance,
            taxableIncome,
            taxOwing,
            netIncome.subtract(taxOwing)
        );
    }
    
    /**
     * Simple data class for tax summary
     */
    public static class TaxSummary {
        public final BigDecimal totalGrossIncome;
        public final BigDecimal totalDeductions;
        public final BigDecimal netIncome;
        public final BigDecimal personalAllowance;
        public final BigDecimal taxableIncome;
        public final BigDecimal taxOwing;
        public final BigDecimal netTakeHome;
        
        public TaxSummary(BigDecimal totalGrossIncome, BigDecimal totalDeductions,
                         BigDecimal netIncome, BigDecimal personalAllowance,
                         BigDecimal taxableIncome, BigDecimal taxOwing,
                         BigDecimal netTakeHome) {
            this.totalGrossIncome = totalGrossIncome;
            this.totalDeductions = totalDeductions;
            this.netIncome = netIncome;
            this.personalAllowance = personalAllowance;
            this.taxableIncome = taxableIncome;
            this.taxOwing = taxOwing;
            this.netTakeHome = netTakeHome;
        }
        
        @Override
        public String toString() {
            return "TaxSummary{\n" +
                "  totalGrossIncome=" + totalGrossIncome + "\n" +
                "  totalDeductions=" + totalDeductions + "\n" +
                "  netIncome=" + netIncome + "\n" +
                "  personalAllowance=" + personalAllowance + "\n" +
                "  taxableIncome=" + taxableIncome + "\n" +
                "  taxOwing=" + taxOwing + "\n" +
                "  netTakeHome=" + netTakeHome + "\n" +
                "}";
        }
    }
}
