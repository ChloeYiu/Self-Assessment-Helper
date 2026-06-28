package com.helper.core.income;

import java.math.BigDecimal;

/**
 * Base interface for all income sources
 */
public interface Income {
    /**
     * Get the income type
     */
    IncomeType getIncomeType();
    
    /**
     * Get the gross income amount
     */
    BigDecimal getGrossIncome();

    /**
     * Get the amount that should feed into wider income calculations after local category rules.
     */
    default BigDecimal getAdjustedIncome() {
        return getGrossIncome();
    }
    
    /**
     * Get the tax year this income applies to
     */
    int getTaxYear();
}
