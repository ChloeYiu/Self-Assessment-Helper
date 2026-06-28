package com.helper.core.income.implementation.savings;

import java.math.BigDecimal;

/**
 * Interface for income sources that contribute to savings income.
 */
public interface Saving {

    /**
     * Gross savings amount in GBP before category-level allowances.
     */
    BigDecimal calculateSavingAmount();

    /**
     * Tax year this income applies to.
     */
    int getTaxYear();
}
