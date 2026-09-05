package com.helper.ingestion.util.api;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility methods for validating date ranges used by ingestion APIs.
 */
public final class DateRangeValidator {
    private DateRangeValidator() {
    }

    public static void requireValidRange(LocalDate fromDate, LocalDate toDate) {
        Objects.requireNonNull(fromDate, "fromDate");
        Objects.requireNonNull(toDate, "toDate");

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException("toDate must not be before fromDate");
        }
    }

    public static void requireMaxDays(LocalDate fromDate, LocalDate toDate, int maxDays) {
        requireValidRange(fromDate, toDate);

        long rangeDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (rangeDays > maxDays) {
            throw new IllegalArgumentException("date range cannot exceed " + maxDays + " days");
        }
    }
}
