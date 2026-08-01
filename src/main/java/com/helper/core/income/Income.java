package com.helper.core.income;

import com.helper.core.config.TaxYear;

/**
 * Base interface for all income sources
 */
public interface Income {
    /**
     * Get the income type
     */
    IncomeType getIncomeType();

    /**
     * Calculate this income and any side-products together
     */
    IncomeResult calculateResult();

    /**
     * Get the tax year this income applies to
     */
    TaxYear getTaxYear();
}
