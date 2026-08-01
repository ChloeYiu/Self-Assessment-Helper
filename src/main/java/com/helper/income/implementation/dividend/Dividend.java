package com.helper.income.implementation.dividend;

import com.helper.config.TaxYear;
import java.math.BigDecimal;

/**
 * Interface for income sources that contribute to the dividend category.
 */
public interface Dividend {

    /**
     * Gross dividend amount before category-level allowance.
     */
    BigDecimal calculateDividendAmount();

     /**
     * Tax year this income applies to.
     */
    TaxYear getTaxYear();
}
