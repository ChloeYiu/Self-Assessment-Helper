package com.helper.core.income.implementation;

import com.helper.core.config.TaxYear;
import com.helper.core.income.implementation.rentaroom.ExpenseType;
import com.helper.core.income.implementation.rentaroom.RentARoomCalculationMethod;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RentARoomIncomeTest {

    @Test
    public void getGrossIncome_returnsRawRentBeforeLocalAdjustment() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);

        assertBigDecimalEquals("15000", income.getGrossIncome());
        assertBigDecimalEquals("7500", income.getAdjustedIncome());
    }

    @Test
    public void getAdjustedIncome_usesAllowanceWhenItIsBetter() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);

        BigDecimal adjustedIncome = income.getAdjustedIncome();

        assertBigDecimalEquals("7500", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void getAdjustedIncome_usesActualExpensesWhenTheyReduceTaxableAmountMore() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("9000"));

        BigDecimal adjustedIncome = income.getAdjustedIncome();

        assertBigDecimalEquals("6000", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ACTUAL_EXPENSES, income.getPreferredCalculationMethod());
    }

    @Test
    public void getAdjustedIncome_returnsZeroWhenAllowanceCoversAllProfit() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("5000"), TaxYear.of(2025), BigDecimal.ONE);

        BigDecimal adjustedIncome = income.getAdjustedIncome();

        assertBigDecimalEquals("0", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void getAdjustedIncome_appliesHouseholdAllocationToExpenses() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), new BigDecimal("0.5"));
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("8000"));

        BigDecimal adjustedIncome = income.getAdjustedIncome();

        assertBigDecimalEquals("7500", adjustedIncome);
        assertEquals(RentARoomCalculationMethod.ALLOWANCE, income.getPreferredCalculationMethod());
    }

    @Test
    public void getAdjustedIncomeWithActualExpenses_optsOutOfRentARoomAllowance() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), new BigDecimal("0.5"));
        income.addExpense(ExpenseType.REPAIRS, new BigDecimal("8000"));

        BigDecimal actualExpenseMethodAdjusted = income.getAdjustedIncomeWithActualExpenses();

        // No rent-a-room allowance is applied: 8000 * 0.5 = 4000 expenses; 15000 - 4000 = 11000.
        assertBigDecimalEquals("11000", actualExpenseMethodAdjusted);
    }

    @Test(expected = NullPointerException.class)
    public void constructor_throwsWhenGrossRentIsNull() {
        new RentARoomIncome(null, TaxYear.of(2025), BigDecimal.ONE);
    }

    @Test(expected = NullPointerException.class)
    public void addExpense_throwsWhenAmountIsNull() {
        RentARoomIncome income = new RentARoomIncome(new BigDecimal("15000"), TaxYear.of(2025), BigDecimal.ONE);

        income.addExpense(ExpenseType.REPAIRS, null);
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
