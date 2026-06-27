package com.helper.core.income.implementation.savings;

import com.helper.core.config.CurrencyCode;
import com.helper.core.config.TaxYearPeriod;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ForeignSavingTest {

    @Test
    public void calculateSavingAmount_prefersYearlyWhenItProducesLowerGbp() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        income.setMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("1000"));

        income.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.12"));
        income.setMonthlyRate(TaxYearPeriod.JUNE, new BigDecimal("0.12"));
        income.setYearlyRate(new BigDecimal("0.10"));

        BigDecimal monthly = income.calculateSavingAmountWithMonthlyRates();
        BigDecimal yearly = income.calculateSavingAmountWithYearlyRate();
        BigDecimal selected = income.calculateSavingAmount();

        assertBigDecimalEquals("240.00", monthly);
        assertBigDecimalEquals("200.00", yearly);
        assertBigDecimalEquals("200.00", selected);
        assertEquals(ForeignSavingsCalculationMethod.YEARLY_AVERAGE, income.getCalculationMethod());
    }

    @Test
    public void calculateSavingAmount_prefersMonthlyWhenItProducesLowerGbp() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        income.setMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("1000"));

        income.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.10"));
        income.setMonthlyRate(TaxYearPeriod.JUNE, new BigDecimal("0.10"));
        income.setYearlyRate(new BigDecimal("0.12"));

        BigDecimal monthly = income.calculateSavingAmountWithMonthlyRates();
        BigDecimal yearly = income.calculateSavingAmountWithYearlyRate();
        BigDecimal selected = income.calculateSavingAmount();

        assertBigDecimalEquals("200.00", monthly);
        assertBigDecimalEquals("240.00", yearly);
        assertBigDecimalEquals("200.00", selected);
        assertEquals(ForeignSavingsCalculationMethod.MONTHLY, income.getCalculationMethod());
    }

    @Test
    public void calculateSavingAmount_usesMonthlyWhenOnlyMonthlyRatesExist() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        income.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.10"));

        assertBigDecimalEquals("100.00", income.calculateSavingAmount());
        assertEquals(ForeignSavingsCalculationMethod.MONTHLY, income.getCalculationMethod());
    }

    @Test
    public void calculateSavingAmount_usesYearlyWhenOnlyYearlyRateExists() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        income.setYearlyRate(new BigDecimal("0.10"));

        assertBigDecimalEquals("100.00", income.calculateSavingAmount());
        assertEquals(ForeignSavingsCalculationMethod.YEARLY_AVERAGE, income.getCalculationMethod());
    }

    @Test(expected = IllegalStateException.class)
    public void calculateSavingAmount_throwsWhenIncomeExistsWithoutAnyRate() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));

        income.calculateSavingAmount();
    }

    @Test
    public void calculateSavingAmountWithMonthlyRates_supportsThirteenTaxYearPeriods() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        for (TaxYearPeriod period : TaxYearPeriod.values()) {
            income.setMonthlyIncome(period, new BigDecimal("100"));
            income.setMonthlyRate(period, new BigDecimal("0.10"));
        }

        BigDecimal monthly = income.calculateSavingAmountWithMonthlyRates();

        assertBigDecimalEquals("130.00", monthly);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_throwsWhenCurrencyCodeIsNull() {
        new ForeignSaving(2025, null);
    }

    @Test(expected = NullPointerException.class)
    public void setMonthlyRate_throwsWhenRateIsNull() {
        ForeignSaving income = new ForeignSaving(2025, CurrencyCode.HKD);

        income.setMonthlyRate(TaxYearPeriod.MAY, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
