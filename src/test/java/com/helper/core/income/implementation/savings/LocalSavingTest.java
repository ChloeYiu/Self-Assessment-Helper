package com.helper.core.income.implementation.savings;

import com.helper.core.config.TaxYearPeriod;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class LocalSavingTest {

    @Test
    public void calculateSavingAmount_returnsZeroWhenNoIncomeExists() {
        LocalSaving income = new LocalSaving(2025);

        assertBigDecimalEquals("0", income.calculateSavingAmount());
        assertEquals(2025, income.getTaxYear());
    }

    @Test
    public void calculateSavingAmount_sumsMonthlyIncomeWhenProvided() {
        LocalSaving income = new LocalSaving(2025);

        income.setMonthlyIncome(TaxYearPeriod.MAY, new BigDecimal("10.25"));
        income.setMonthlyIncome(TaxYearPeriod.JUNE, new BigDecimal("12.75"));

        assertBigDecimalEquals("23.00", income.calculateSavingAmount());
    }

    @Test(expected = NullPointerException.class)
    public void setMonthlyIncome_throwsWhenAmountIsNull() {
        LocalSaving income = new LocalSaving(2025);

        income.setMonthlyIncome(TaxYearPeriod.MAY, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
