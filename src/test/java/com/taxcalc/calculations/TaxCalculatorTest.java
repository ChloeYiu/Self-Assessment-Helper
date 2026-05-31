package com.taxcalc.calculations;

import com.taxcalc.income.Income;
import com.taxcalc.income.IncomeType;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TaxCalculatorTest {

    @Test
    public void calculateTaxByType_returnsZeroWhenNoSourcesForType() {
        TaxCalculator calculator = new TaxCalculator(2025);

        BigDecimal result = calculator.calculateTaxByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("0", result);
    }

    @Test
    public void calculateTaxByType_sumsTaxableAmountsForSameType() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(IncomeType.SAVINGS, createIncomeMock(IncomeType.SAVINGS, "100"));
        calculator.addIncomeSources(IncomeType.SAVINGS, createIncomeMock(IncomeType.SAVINGS, "250.50"));

        BigDecimal result = calculator.calculateTaxByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("350.50", result);
    }

    @Test
    public void calculateTaxByType_onlyUsesRequestedType() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(IncomeType.SAVINGS, createIncomeMock(IncomeType.SAVINGS, "100"));
        calculator.addIncomeSources(IncomeType.DIVIDENDS, createIncomeMock(IncomeType.DIVIDENDS, "400"));

        BigDecimal result = calculator.calculateTaxByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("100", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncomeSources_throwsWhenIncomeTypeIsNull() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(null, createIncomeMock(IncomeType.SAVINGS, "100"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncomeSources_throwsWhenSourceIsNull() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(IncomeType.SAVINGS, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static Income createIncomeMock(IncomeType incomeType, String taxableAmount) {
        Income income = mock(Income.class);
        when(income.getIncomeType()).thenReturn(incomeType);
        when(income.calculateTaxableAmount()).thenReturn(new BigDecimal(taxableAmount));
        when(income.getGrossIncome()).thenReturn(new BigDecimal(taxableAmount));
        when(income.getTaxYear()).thenReturn(2025);
        return income;
    }
}
