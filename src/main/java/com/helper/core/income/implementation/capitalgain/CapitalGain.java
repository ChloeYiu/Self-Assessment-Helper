package com.helper.core.income.implementation.capitalgain;

import java.math.BigDecimal;

/**
 * Interface for income sources that contribute to the capital gain category.
 */
public interface CapitalGain {

    /**
     * Gross capital gain amount in GBP before category-level allowance.
     */
    BigDecimal calculateCapitalGainAmount();

    /**
     * Tax year this gain applies to.
     */
    int getTaxYear();
}
