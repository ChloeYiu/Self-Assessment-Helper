package com.taxcalc.fx;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Contract for obtaining FX rates.
 */
public interface FxRateProvider {

    /**
     * Get a monthly FX rate for a currency and month.
     */
    BigDecimal getMonthlyRate(String currencyCode, YearMonth month) throws IOException, InterruptedException;

    /**
     * Get a yearly-average FX rate for a currency and month key.
     */
    BigDecimal getYearlyAverageRate(String currencyCode, YearMonth month) throws IOException, InterruptedException;
}
