package com.helper.ingestion.util.api;

import java.time.LocalDate;
import org.junit.Test;

import static org.junit.Assert.assertThrows;

public class DateRangeValidatorTest {

    @Test
    public void requireMaxDays_acceptsRangeAtLimit() {
        DateRangeValidator.requireMaxDays(
                LocalDate.of(2025, 4, 6),
                LocalDate.of(2025, 4, 10),
                5);
    }

    @Test
    public void requireMaxDays_rejectsRangeOverLimit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DateRangeValidator.requireMaxDays(
                        LocalDate.of(2025, 4, 6),
                        LocalDate.of(2025, 4, 11),
                        5));
    }

    @Test
    public void requireValidRange_rejectsEndDateBeforeStartDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> DateRangeValidator.requireValidRange(
                        LocalDate.of(2025, 4, 7),
                        LocalDate.of(2025, 4, 6)));
    }
}
