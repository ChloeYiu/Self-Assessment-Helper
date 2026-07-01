package com.helper.core.config;

import java.time.LocalDate;
import java.time.Month;
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
