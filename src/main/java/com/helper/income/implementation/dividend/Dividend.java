package com.helper.income.implementation.dividend;

import com.helper.config.TaxYear;
import com.helper.income.implementation.dividend.model.DividendResult;

/**
 * Interface for income sources that contribute to the dividend category.
 */
public interface Dividend {

    /**
     * Calculates the dividend result before category-level allowance.
     */
    DividendResult calculateDividendResult();

     /**
     * Tax year this income applies to.
     */
    TaxYear getTaxYear();
}
