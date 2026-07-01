package com.helper.core.income.implementation.dividend;

import com.helper.core.config.TaxYear;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Models reported managed-trade dividend income.
 */
public class ManagedTradeDividend implements Dividend {

    public TaxYear taxYear;
    private final BigDecimal dividendAmount;

    public ManagedTradeDividend(TaxYear taxYear, BigDecimal dividendAmount) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.dividendAmount = Objects.requireNonNull(dividendAmount, "dividendAmount");
    }

    @Override
    public BigDecimal calculateDividendAmount() {
        return dividendAmount;
    }

    @Override
    public TaxYear getTaxYear() {
        return taxYear;
    }
}
