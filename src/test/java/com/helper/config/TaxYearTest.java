package com.helper.config;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaxYearTest {

    @Test
    public void contains_treatsStartYearAsUkTaxYearStart() {
        TaxYear taxYear = TaxYear.of(2026);

        assertEquals(LocalDate.of(2026, 4, 6), taxYear.getStartDate());
        assertEquals(LocalDate.of(2027, 4, 5), taxYear.getEndDate());
        assertTrue(taxYear.contains(LocalDate.of(2026, 4, 6)));
        assertTrue(taxYear.contains(LocalDate.of(2027, 4, 5)));
        assertFalse(taxYear.contains(LocalDate.of(2026, 4, 5)));
        assertFalse(taxYear.contains(LocalDate.of(2027, 4, 6)));
    }

    @Test
    public void fromDate_returnsContainingTaxYear() {
        assertEquals(2025, TaxYear.fromDate(LocalDate.of(2026, 4, 5)).getStartYear());
        assertEquals(2026, TaxYear.fromDate(LocalDate.of(2026, 4, 6)).getStartYear());
    }

    @Test
    public void getPeriodForDate_mapsDateToTaxYearPeriod() {
        TaxYear taxYear = TaxYear.of(2025);

        assertEquals(TaxYearPeriod.APRIL_6_TO_30, taxYear.getPeriodForDate(LocalDate.of(2025, 4, 6)));
        assertEquals(TaxYearPeriod.MAY, taxYear.getPeriodForDate(LocalDate.of(2025, 5, 1)));
        assertEquals(TaxYearPeriod.JANUARY, taxYear.getPeriodForDate(LocalDate.of(2026, 1, 15)));
        assertEquals(TaxYearPeriod.APRIL_1_TO_5, taxYear.getPeriodForDate(LocalDate.of(2026, 4, 5)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPeriodForDate_throwsWhenDateIsOutsideTaxYear() {
        TaxYear.of(2025).getPeriodForDate(LocalDate.of(2025, 4, 5));
    }
}
