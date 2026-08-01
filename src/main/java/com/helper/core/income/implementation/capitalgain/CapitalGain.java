package com.helper.core.income.implementation.capitalgain;

import com.helper.core.config.TaxYear;
import com.helper.core.income.implementation.capitalgain.model.CapitalGainResult;

/**
 * Interface for income sources that contribute to the capital gain category.
 */
public interface CapitalGain {

    CapitalGainResult calculateCapitalGain();

     /**
     * Tax year this gain applies to.
     */
    TaxYear getTaxYear();
}
