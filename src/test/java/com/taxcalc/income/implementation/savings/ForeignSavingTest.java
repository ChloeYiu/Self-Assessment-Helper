package com.taxcalc.income.implementation.savings;

import com.taxcalc.config.TaxYearPeriod;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ForeignSavingTest {

    @Test
    public void calculateSavingAmount_prefersMonthlyWhenItProducesHigherGbp() {
        ForeignSaving income = new ForeignSaving(2025, "EUR");

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        income.setMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("1000"));

        income.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("1.25"));
        income.setMonthlyRate(TaxYearPeriod.JUNE, new BigDecimal("1.25"));
        income.setYearlyRate(new BigDecimal("1.10"));

        BigDecimal monthly = income.calculateSavingAmountWithMonthlyRates();
        BigDecimal yearly = income.calculateSavingAmountWithYearlyRate();
        BigDecimal selected = income.calculateSavingAmount();

        assertBigDecimalEquals("2500.00", monthly);
        assertBigDecimalEquals("2200.00", yearly);
        assertBigDecimalEquals("2500.00", selected);
        assertEquals(ForeignSavingsCalculationMethod.MONTHLY, income.getCalculationMethod());
    }

    @Test
    public void calculateSavingAmount_prefersYearlyWhenItProducesHigherGbp() {
        ForeignSaving income = new ForeignSaving(2025, "EUR");

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        income.setMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("1000"));

        income.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("1.10"));
        income.setMonthlyRate(TaxYearPeriod.JUNE, new BigDecimal("1.10"));
        income.setYearlyRate(new BigDecimal("1.25"));

        BigDecimal monthly = income.calculateSavingAmountWithMonthlyRates();
        BigDecimal yearly = income.calculateSavingAmountWithYearlyRate();
        BigDecimal selected = income.calculateSavingAmount();

        assertBigDecimalEquals("2200.00", monthly);
        assertBigDecimalEquals("2500.00", yearly);
        assertBigDecimalEquals("2500.00", selected);
        assertEquals(ForeignSavingsCalculationMethod.YEARLY_AVERAGE, income.getCalculationMethod());
    }

    @Test
    public void calculateSavingAmountWithMonthlyRates_supportsThirteenTaxYearPeriods() {
        ForeignSaving income = new ForeignSaving(2025, "EUR");

        for (TaxYearPeriod period : TaxYearPeriod.values()) {
            income.setMonthlyIncome(period, new BigDecimal("100"));
            income.setMonthlyRate(period, new BigDecimal("1.25"));
        }

        BigDecimal monthly = income.calculateSavingAmountWithMonthlyRates();

        assertBigDecimalEquals("1625.00", monthly);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
