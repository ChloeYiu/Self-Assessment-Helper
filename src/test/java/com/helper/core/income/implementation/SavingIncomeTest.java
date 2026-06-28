package com.helper.core.income.implementation;

import com.helper.core.config.TaxYearPeriod;
import com.helper.core.income.IncomeType;
import com.helper.core.income.implementation.savings.LocalSaving;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class SavingIncomeTest {

    @Test
    public void getGrossIncome_sumsLocalSavings() {
        LocalSaving localSaving = new LocalSaving(2025);
        localSaving.addMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("100.25"));
        localSaving.addMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("50.75"));

        SavingIncome income = new SavingIncome(2025);
        income.addSavingIncome(localSaving);

        assertEquals(IncomeType.SAVINGS, income.getIncomeType());
        assertEquals(2025, income.getTaxYear());
        assertBigDecimalEquals("151.00", income.getGrossIncome());
        assertBigDecimalEquals("151.00", income.getAdjustedIncome());
    }

    @Test(expected = NullPointerException.class)
    public void addSavingIncome_throwsWhenSourceIsNull() {
        SavingIncome income = new SavingIncome(2025);

        income.addSavingIncome(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSavingIncome_throwsWhenSourceTaxYearDoesNotMatch() {
        SavingIncome income = new SavingIncome(2025);
        LocalSaving foreignSaving = new LocalSaving(2024);

        income.addSavingIncome(foreignSaving);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
