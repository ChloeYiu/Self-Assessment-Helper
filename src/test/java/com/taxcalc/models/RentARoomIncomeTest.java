package com.taxcalc.models;

import com.taxcalc.config.ExpenseType;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RentARoomIncomeTest {

    @Test
    public void calculateTaxableAmount_usesAllowanceWhenItIsBetter() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), 2025, BigDecimal.ONE, "");

        BigDecimal taxableAmount = income.calculateTaxableAmount();

        assertBigDecimalEquals("7500", taxableAmount);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateTaxableAmount_usesActualExpensesWhenTheyReduceTaxableAmountMore() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), 2025, new BigDecimal("1"), "");
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("9000"));

        BigDecimal taxableAmount = income.calculateTaxableAmount();

        assertBigDecimalEquals("6000", taxableAmount);
        assertEquals(RentARoomCalculationMethod.ACTUAL_EXPENSES, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateTaxableAmount_returnsZeroWhenAllowanceCoversAllProfit() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("5000"), 2025, BigDecimal.ONE, "");

        BigDecimal taxableAmount = income.calculateTaxableAmount();

        assertBigDecimalEquals("0", taxableAmount);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateTaxableAmount_appliesHouseholdAllocationToExpenses() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), 2025, new BigDecimal("0.5"), "shared property");
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("8000"));

        BigDecimal taxableAmount = income.calculateTaxableAmount();

        assertBigDecimalEquals("7500", taxableAmount);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void calculateTaxableAmountWithActualExpenses_appliesHouseholdAllocationPercentage() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), 2025, new BigDecimal("0.5"), "shared property");
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("8000"));

        BigDecimal actualExpenseMethodTaxable = income.calculateTaxableAmountWithActualExpenses();

        // 8000 * 0.5 = 4000 allocated expenses; 15000 - 4000 = 11000 taxable
        assertBigDecimalEquals("11000", actualExpenseMethodTaxable);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
