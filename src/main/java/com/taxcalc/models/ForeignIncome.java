package com.taxcalc.models;

import java.math.BigDecimal;

/**
 * Models foreign income (employment, self-employment, investment, etc.)
 */
public class ForeignIncome implements IncomeSource {
    
    public BigDecimal foreignGrossIncome;
    public BigDecimal ukTaxPaid;
    public String incomeType; // e.g., "Employment", "Self-employment", "Investment"
    public String country;
    public String exchangeRateUsed;
    public int taxYear;
    public String notes;
    
    public ForeignIncome(BigDecimal foreignGrossIncome, String incomeType, 
                        String country, int taxYear) {
        this.foreignGrossIncome = foreignGrossIncome;
        this.incomeType = incomeType;
        this.country = country;
        this.taxYear = taxYear;
        this.ukTaxPaid = BigDecimal.ZERO;
        this.notes = "";
    }
    
    @Override
    public IncomeType getIncomeType() {
        return IncomeType.FOREIGN;
    }
    
    @Override
    public BigDecimal getGrossIncome() {
        return foreignGrossIncome;
    }
    
    @Override
    public int getTaxYear() {
        return taxYear;
    }
    
    @Override
    public BigDecimal calculateTaxableAmount() {
        // Foreign income included in UK tax return
        return foreignGrossIncome;
    }
    
}
