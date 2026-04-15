package com.taxcalc.models;

import java.math.BigDecimal;

/**
 * Base interface for all income sources
 */
public interface IncomeSource {
    
    enum IncomeType {
        RENT_A_ROOM,
        FOREIGN,
        SAVINGS_INTEREST,
        ETF_DIVIDEND,
        MANAGED_INVESTMENT
    }
    
    /**
     * Get the income type
     */
    IncomeType getIncomeType();
    
    /**
     * Get the gross income amount
     */
    BigDecimal getGrossIncome();
    
    /**
     * Get the tax year this income applies to
     */
    int getTaxYear();
    
    /**
     * Calculate the taxable amount for this income source
     * Different income types may have different calculations (e.g., deductions, exemptions, etc.)
     */
    BigDecimal calculateTaxableAmount();
    
}
