package com.helper.core.calculations;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class AllowanceCalculatorTest {

    private final AllowanceCalculator allowanceCalculator = new AllowanceCalculator();

    @Test
    public void getPersonalSavingsAllowance_returnsBasicRateAllowance() {
        assertBigDecimalEquals("1000", allowanceCalculator.getPersonalSavingsAllowance(new BigDecimal("50270")));
    }

    @Test
    public void getPersonalSavingsAllowance_returnsHigherRateAllowance() {
        assertBigDecimalEquals("500", allowanceCalculator.getPersonalSavingsAllowance(new BigDecimal("50270.01")));
    }

    @Test
    public void getPersonalSavingsAllowance_returnsAdditionalRateAllowance() {
        assertBigDecimalEquals("0", allowanceCalculator.getPersonalSavingsAllowance(new BigDecimal("125140.01")));
    }

    @Test(expected = NullPointerException.class)
    public void getPersonalSavingsAllowance_throwsWhenTotalIncomeIsNull() {
        allowanceCalculator.getPersonalSavingsAllowance(null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
