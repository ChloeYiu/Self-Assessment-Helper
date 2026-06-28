package com.helper.core.income.implementation;

import com.helper.core.config.CurrencyCode;
import com.helper.core.config.TaxYearPeriod;
import com.helper.core.income.IncomeType;
import com.helper.core.income.implementation.savings.ForeignSaving;
import com.helper.core.income.implementation.savings.LocalSaving;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class SavingIncomeTest {

    @Test
    public void getGrossIncome_sumsLocalAndForeignSavings() {
        LocalSaving localSaving = new LocalSaving(2025);
        localSaving.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("100.25"));
        localSaving.setMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("50.75"));

        ForeignSaving foreignSaving = new ForeignSaving(2025, CurrencyCode.HKD);
        foreignSaving.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        foreignSaving.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.10"));

        SavingIncome income = new SavingIncome(2025);
        income.addSavingIncome(localSaving);
        income.addSavingIncome(foreignSaving);

        assertEquals(IncomeType.SAVINGS, income.getIncomeType());
        assertEquals(2025, income.getTaxYear());
        assertBigDecimalEquals("251.00", income.getGrossIncome());
    }

    @Test
    public void calculateTaxableAmount_appliesSavingsAllowanceToCombinedSavings() {
        LocalSaving localSaving = new LocalSaving(2025);
        localSaving.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1200"));

        ForeignSaving foreignSaving = new ForeignSaving(2025, CurrencyCode.HKD);
        foreignSaving.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1000"));
        foreignSaving.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.10"));

        SavingIncome income = new SavingIncome(2025);
        income.addSavingIncome(localSaving);
        income.addSavingIncome(foreignSaving);

        assertBigDecimalEquals("300.00", income.calculateTaxableAmount());
    }

    @Test
    public void calculateTaxableAmount_usesTotalIncomeForSavingsAllowance() {
        LocalSaving localSaving = new LocalSaving(2025);
        localSaving.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("1200"));

        SavingIncome income = new SavingIncome(2025);
        income.addSavingIncome(localSaving);

        assertBigDecimalEquals("700", income.calculateTaxableAmount(new BigDecimal("60000")));
    }

    @Test(expected = NullPointerException.class)
    public void addSavingIncome_throwsWhenSourceIsNull() {
        SavingIncome income = new SavingIncome(2025);

        income.addSavingIncome(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSavingIncome_throwsWhenSourceTaxYearDoesNotMatch() {
        SavingIncome income = new SavingIncome(2025);
        ForeignSaving foreignSaving = new ForeignSaving(2024, CurrencyCode.HKD);

        income.addSavingIncome(foreignSaving);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
