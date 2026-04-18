package com.taxcalc.fx;

import com.taxcalc.config.TaxYearPeriod;
import com.taxcalc.income.implementation.savings.ForeignSaving;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * Service that fetches and applies FX rates.
 */
public class FxRateService {

    private final FxRateProvider fxRateProvider;

    public FxRateService(FxRateProvider fxRateProvider) {
        this.fxRateProvider = fxRateProvider;
    }

    /**
     * Populate monthly and yearly-average rates for a foreign savings income record.
     */
    public void populateRates(ForeignSaving income) throws IOException, InterruptedException {
        String currencyCode = income.currencyCode;
        if (currencyCode == null || currencyCode.isBlank()) {
            throw new IllegalArgumentException("currencyCode is required");
        }

        int taxYear = income.getTaxYear();
        for (TaxYearPeriod period : TaxYearPeriod.values()) {
            YearMonth month = mapPeriodToYearMonth(period, taxYear);
            BigDecimal rate = fxRateProvider.getMonthlyRate(currencyCode, month);
            income.setMonthlyRate(period, rate);
        }

        // Anchor yearly average lookup at April of the tax-year end.
        YearMonth yearlyAverageMonth = YearMonth.of(taxYear, 4);
        BigDecimal yearlyRate = fxRateProvider.getYearlyAverageRate(currencyCode, yearlyAverageMonth);
        income.setYearlyRate(yearlyRate);
    }

    YearMonth mapPeriodToYearMonth(TaxYearPeriod period, int taxYear) {
        int startYear = taxYear - 1;
        switch (period) {
            case APRIL_6_TO_30:
                return YearMonth.of(startYear, 4);
            case MAY:
                return YearMonth.of(startYear, 5);
            case JUNE:
                return YearMonth.of(startYear, 6);
            case JULY:
                return YearMonth.of(startYear, 7);
            case AUGUST:
                return YearMonth.of(startYear, 8);
            case SEPTEMBER:
                return YearMonth.of(startYear, 9);
            case OCTOBER:
                return YearMonth.of(startYear, 10);
            case NOVEMBER:
                return YearMonth.of(startYear, 11);
            case DECEMBER:
                return YearMonth.of(startYear, 12);
            case JANUARY:
                return YearMonth.of(taxYear, 1);
            case FEBRUARY:
                return YearMonth.of(taxYear, 2);
            case MARCH:
                return YearMonth.of(taxYear, 3);
            case APRIL_1_TO_5:
                return YearMonth.of(taxYear, 4);
            default:
                throw new IllegalArgumentException("Unsupported period: " + period);
        }
    }
}
