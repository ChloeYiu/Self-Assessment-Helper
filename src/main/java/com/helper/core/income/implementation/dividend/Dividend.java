package com.helper.core.income.implementation.dividend;

import java.math.BigDecimal;

/**
 * Interface for income sources that contribute to the dividend category.
 */
public interface Dividend {

    /**
     * Gross dividend amount in GBP before category-level allowance.
     */
    BigDecimal calculateDividendAmount();

    /**
     * Tax year this income applies to.
     */
    int getTaxYear();
}
