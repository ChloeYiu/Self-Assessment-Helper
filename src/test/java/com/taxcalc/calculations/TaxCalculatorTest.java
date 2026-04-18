package com.taxcalc.calculations;

import com.taxcalc.income.Income;
import com.taxcalc.income.IncomeType;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

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
        calculator.addIncomeSources(IncomeType.SAVINGS, new FixedIncome(IncomeType.SAVINGS, "100"));
        calculator.addIncomeSources(IncomeType.SAVINGS, new FixedIncome(IncomeType.SAVINGS, "250.50"));

        BigDecimal result = calculator.calculateTaxByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("350.50", result);
    }

    @Test
    public void calculateTaxByType_onlyUsesRequestedType() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(IncomeType.SAVINGS, new FixedIncome(IncomeType.SAVINGS, "100"));
        calculator.addIncomeSources(IncomeType.DIVIDENDS, new FixedIncome(IncomeType.DIVIDENDS, "400"));

        BigDecimal result = calculator.calculateTaxByType(IncomeType.SAVINGS);

        assertBigDecimalEquals("100", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncomeSources_throwsWhenIncomeTypeIsNull() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(null, new FixedIncome(IncomeType.SAVINGS, "100"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIncomeSources_throwsWhenSourceIsNull() {
        TaxCalculator calculator = new TaxCalculator(2025);
        calculator.addIncomeSources(IncomeType.SAVINGS, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static class FixedIncome implements Income {
        private final IncomeType incomeType;
        private final BigDecimal taxableAmount;

        FixedIncome(IncomeType incomeType, String taxableAmount) {
            this.incomeType = incomeType;
            this.taxableAmount = new BigDecimal(taxableAmount);
        }

        @Override
        public IncomeType getIncomeType() {
            return incomeType;
        }

        @Override
        public BigDecimal getGrossIncome() {
            return taxableAmount;
        }

        @Override
        public int getTaxYear() {
            return 2025;
        }

        @Override
        public BigDecimal calculateTaxableAmount() {
            return taxableAmount;
        }
    }
}
