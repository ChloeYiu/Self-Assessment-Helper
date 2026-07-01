package com.helper.core.config;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class TaxYearPeriodTest {

    @Test
    public void fromDate_mapsCalendarDateToTaxYearPeriod() {
        assertEquals(TaxYearPeriod.APRIL_1_TO_5, TaxYearPeriod.fromDate(LocalDate.of(2026, 4, 5)));
        assertEquals(TaxYearPeriod.APRIL_6_TO_30, TaxYearPeriod.fromDate(LocalDate.of(2026, 4, 6)));
        assertEquals(TaxYearPeriod.MAY, TaxYearPeriod.fromDate(LocalDate.of(2026, 5, 31)));
        assertEquals(TaxYearPeriod.JANUARY, TaxYearPeriod.fromDate(LocalDate.of(2027, 1, 1)));
    }
}
