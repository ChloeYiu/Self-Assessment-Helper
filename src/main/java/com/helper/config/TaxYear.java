package com.helper.config;

import java.time.LocalDate;
import java.util.Objects;

public class TaxYear {
    private final int startYear;

    private TaxYear(int startYear) {
        this.startYear = startYear;
    }

    public static TaxYear of(int startYear) {
        return new TaxYear(startYear);
    }

    public static TaxYear fromDate(LocalDate date) {
        LocalDate value = Objects.requireNonNull(date, "date");

        if (value.isBefore(LocalDate.of(value.getYear(), 4, 6))) {
            return new TaxYear(value.getYear() - 1);
        }

        return new TaxYear(value.getYear());
    }

    public int getStartYear() {
        return startYear;
    }

    public LocalDate getStartDate() {
        return LocalDate.of(startYear, 4, 6);
    }

    public LocalDate getEndDate() {
        return LocalDate.of(startYear + 1, 4, 5);
    }

    public boolean contains(LocalDate date) {
        LocalDate value = Objects.requireNonNull(date, "date");

        return !value.isBefore(getStartDate()) && !value.isAfter(getEndDate());
    }

    public TaxYearPeriod getPeriodForDate(LocalDate date) {
        LocalDate value = Objects.requireNonNull(date, "date");
        if (!contains(value)) {
            throw new IllegalArgumentException("date must be within the tax year");
        }

        return TaxYearPeriod.fromDate(value);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TaxYear)) {
            return false;
        }
        TaxYear taxYear = (TaxYear) other;
        return startYear == taxYear.startYear;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startYear);
    }
}
