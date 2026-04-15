package com.taxcalc.calculations;

import java.math.BigDecimal;

/**
 * Represents tax allowances and bands for a given tax year
 */
public class TaxAllowance {
    
    public int taxYear;
    public BigDecimal personalAllowance;
    public BigDecimal basicRateBandLimit; // Upper limit of basic rate band
    public BigDecimal higherRateBandLimit; // Upper limit of higher rate band
    
    public BigDecimal basicRatePercentage;
    public BigDecimal higherRatePercentage;
    public BigDecimal additionalRatePercentage;
    
    public BigDecimal personalSavingsAllowanceBasicRate;
    public BigDecimal personalSavingsAllowanceHigherRate;
    
    public BigDecimal dividendAllowance;
    public BigDecimal capitalGainsAllowance;
    
    public TaxAllowance(int taxYear) {
        this.taxYear = taxYear;
        initializeTaxRatesForYear(taxYear);
    }
    
    private void initializeTaxRatesForYear(int taxYear) {
        // Example: 2024/25 tax year
        if (taxYear == 2024 || taxYear == 2025) {
            this.personalAllowance = BigDecimal.valueOf(12570);
            this.basicRateBandLimit = BigDecimal.valueOf(50270);
            this.higherRateBandLimit = BigDecimal.valueOf(125140);
            
            this.basicRatePercentage = BigDecimal.valueOf(20);
            this.higherRatePercentage = BigDecimal.valueOf(40);
            this.additionalRatePercentage = BigDecimal.valueOf(45);
            
            this.personalSavingsAllowanceBasicRate = BigDecimal.valueOf(1000);
            this.personalSavingsAllowanceHigherRate = BigDecimal.valueOf(500);
            
            this.dividendAllowance = BigDecimal.valueOf(500);
            this.capitalGainsAllowance = BigDecimal.valueOf(3000);
        } else {
            // Default to current rates
            this.personalAllowance = BigDecimal.valueOf(12570);
            this.basicRateBandLimit = BigDecimal.valueOf(50270);
            this.higherRateBandLimit = BigDecimal.valueOf(125140);
            this.basicRatePercentage = BigDecimal.valueOf(20);
            this.higherRatePercentage = BigDecimal.valueOf(40);
            this.additionalRatePercentage = BigDecimal.valueOf(45);
            this.personalSavingsAllowanceBasicRate = BigDecimal.valueOf(1000);
            this.personalSavingsAllowanceHigherRate = BigDecimal.valueOf(500);
            this.dividendAllowance = BigDecimal.valueOf(500);
            this.capitalGainsAllowance = BigDecimal.valueOf(3000);
        }
    }

    public BigDecimal getPersonalAllowance() {
        return personalAllowance;
    }
    
}
