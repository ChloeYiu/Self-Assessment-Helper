package com.helper.core.income.implementation.dividend;

import com.helper.core.config.TaxYear;
import java.math.BigDecimal;

/**
 * Template model for non-UK domicile accumulating dividend income.
 */
public class NonUkDomicileAccumulatingDividend implements Dividend {

    public NonUkDomicileAccumulatingDividend(int taxYear, String fundName, String domicileCountry, BigDecimal grossDividend) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal calculateDividendAmount() {
        throw new UnsupportedOperationException();
    }

    @Override
    public TaxYear getTaxYear() {
        throw new UnsupportedOperationException();
    }
}
