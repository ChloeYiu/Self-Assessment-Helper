package com.helper.config;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.util.Objects;

public enum TaxYearPeriod {
    APRIL_6_TO_30,
    MAY,
    JUNE,
    JULY,
    AUGUST,
    SEPTEMBER,
    OCTOBER,
    NOVEMBER,
    DECEMBER,
    JANUARY,
    FEBRUARY,
    MARCH,
    APRIL_1_TO_5;

    public YearMonth toYearMonth(TaxYear taxYear) {
        int startYear = Objects.requireNonNull(taxYear, "taxYear").getStartYear();
        switch (this) {
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
                return YearMonth.of(startYear + 1, 1);
            case FEBRUARY:
                return YearMonth.of(startYear + 1, 2);
            case MARCH:
                return YearMonth.of(startYear + 1, 3);
            case APRIL_1_TO_5:
                return YearMonth.of(startYear + 1, 4);
            default:
                throw new IllegalStateException("Unexpected tax year period: " + this);
        }
    }

    public static TaxYearPeriod fromDate(LocalDate date) {
        LocalDate value = Objects.requireNonNull(date, "date");
        Month month = value.getMonth();

        switch (month) {
            case JANUARY:
                return JANUARY;
            case FEBRUARY:
                return FEBRUARY;
            case MARCH:
                return MARCH;
            case APRIL:
                return value.getDayOfMonth() <= 5 ? APRIL_1_TO_5 : APRIL_6_TO_30;
            case MAY:
                return MAY;
            case JUNE:
                return JUNE;
            case JULY:
                return JULY;
            case AUGUST:
                return AUGUST;
            case SEPTEMBER:
                return SEPTEMBER;
            case OCTOBER:
                return OCTOBER;
            case NOVEMBER:
                return NOVEMBER;
            case DECEMBER:
                return DECEMBER;
            default:
                throw new IllegalArgumentException("Unsupported month: " + month);
        }
    }
}
