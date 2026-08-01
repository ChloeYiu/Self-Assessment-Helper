package com.helper.core.income.implementation;

import com.helper.core.config.TaxYear;
import com.helper.core.config.TaxYearPeriod;
import com.helper.core.income.IncomeResult;
import com.helper.core.income.IncomeType;
import com.helper.core.income.implementation.savings.LocalSaving;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class SavingIncomeTest {

    @Test
    public void calculateResult_sumsLocalSavings() {
        LocalSaving localSaving = new LocalSaving(TaxYear.of(2025));
        localSaving.addMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("100.25"));
        localSaving.addMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("50.75"));

        SavingIncome income = new SavingIncome(TaxYear.of(2025));
        income.addSource(localSaving);
        IncomeResult result = income.calculateResult();

        assertEquals(IncomeType.SAVINGS, income.getIncomeType());
        assertEquals(TaxYear.of(2025), income.getTaxYear());
        assertBigDecimalEquals("151.00", result.getGrossIncome());
        assertBigDecimalEquals("151.00", result.getAdjustedIncome());
    }

    @Test(expected = NullPointerException.class)
    public void addSource_throwsWhenSourceIsNull() {
        SavingIncome income = new SavingIncome(TaxYear.of(2025));

        income.addSource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSource_throwsWhenSourceTaxYearDoesNotMatch() {
        SavingIncome income = new SavingIncome(TaxYear.of(2025));
        LocalSaving foreignSaving = new LocalSaving(TaxYear.of(2024));

        income.addSource(foreignSaving);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
