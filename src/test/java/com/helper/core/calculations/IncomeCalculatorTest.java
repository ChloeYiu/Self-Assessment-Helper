package com.helper.core.calculations;

import com.helper.core.config.TaxYear;
import com.helper.core.income.Income;
import com.helper.core.income.IncomeType;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class IncomeCalculatorTest {

    @Test
    public void calculateAdjustedIncomeByType_returnsZeroWhenNoSourcesForType() {
        IncomeCalculator calculator = new IncomeCalculator(TaxYear.of(2025));

        BigDecimal result = calculator.calculateAdjustedIncomeByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("0", result);
    }

    @Test
    public void calculateAdjustedIncomeByType_sumsAdjustedIncomeForSameType() {
        IncomeCalculator calculator = new IncomeCalculator(TaxYear.of(2025));
        calculator.addIncomeSources(IncomeType.SAVINGS, createIncomeMock(IncomeType.SAVINGS, "100", "80"));
        calculator.addIncomeSources(IncomeType.SAVINGS, createIncomeMock(IncomeType.SAVINGS, "250.50", "200"));

        BigDecimal result = calculator.calculateAdjustedIncomeByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("280", result);
    }

    @Test
    public void calculateAdjustedIncomeByType_onlyUsesRequestedType() {
        IncomeCalculator calculator = new IncomeCalculator(TaxYear.of(2025));
        calculator.addIncomeSources(IncomeType.SAVINGS, createIncomeMock(IncomeType.SAVINGS, "100", "80"));
        calculator.addIncomeSources(IncomeType.DIVIDENDS, createIncomeMock(IncomeType.DIVIDENDS, "400", "350"));

        BigDecimal result = calculator.calculateAdjustedIncomeByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("80", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncomeSources_throwsWhenIncomeTypeIsNull() {
        IncomeCalculator calculator = new IncomeCalculator(TaxYear.of(2025));
        calculator.addIncomeSources(null, createIncomeMock(IncomeType.SAVINGS, "100", "80"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncomeSources_throwsWhenSourceIsNull() {
        IncomeCalculator calculator = new IncomeCalculator(TaxYear.of(2025));
        calculator.addIncomeSources(IncomeType.SAVINGS, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static Income createIncomeMock(IncomeType incomeType, String grossIncome, String adjustedIncome) {
        Income income = mock(Income.class);
        when(income.getIncomeType()).thenReturn(incomeType);
        when(income.getGrossIncome()).thenReturn(new BigDecimal(grossIncome));
        when(income.getAdjustedIncome()).thenReturn(new BigDecimal(adjustedIncome));
        when(income.getTaxYear()).thenReturn(TaxYear.of(2025));
        return income;
    }
}
