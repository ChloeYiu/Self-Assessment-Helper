package com.helper.core.income.implementation.dividend;

import com.helper.core.config.CurrencyCode;
import com.helper.core.config.TaxYear;
import com.helper.core.config.TaxYearPeriod;
import com.helper.core.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.core.income.implementation.dividend.model.HoldingMovementType;
import com.helper.core.security.Security;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.Assert.assertEquals;

public class NonUkDomicileAccumulatingDividendTest {

    @Test
    public void calculateDividendAmount_usesHoldingAtReportingPeriodEnd() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addHoldingMovement(LocalDate.of(2025, 1, 15), new BigDecimal("2"), HoldingMovementType.BUY);
        dividend.addHoldingMovement(LocalDate.of(2025, 8, 1), new BigDecimal("1"), HoldingMovementType.SELL);

        assertBigDecimalEquals("22.00", dividend.calculateDividendAmount());
        assertEquals(TaxYear.of(2026), dividend.getTaxYear());
        assertEquals("IE00B6R52259", dividend.getFundIdentifier());
        assertEquals("iShares MSCI ACWI UCITS ETF", dividend.getFundName());
        assertEquals("ACWI", dividend.getTicker());
        assertEquals(CurrencyCode.GBP, dividend.getTradingCurrencyCode());
        assertEquals(CurrencyCode.USD, dividend.getReportCurrencyCode());
    }

    @Test
    public void calculateDividendAmountInGbp_prefersYearlyWhenItProducesLowerAmount() {
        NonUkDomicileAccumulatingDividend dividend = createAcwiDividend();

        dividend.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.90"));
        dividend.setYearlyRate(new BigDecimal("0.80"));

        assertBigDecimalEquals("19.80", dividend.calculateDividendAmountInGbpWithMonthlyRate());
        assertBigDecimalEquals("17.60", dividend.calculateDividendAmountInGbpWithYearlyRate());
        assertBigDecimalEquals("17.60", dividend.calculateDividendAmountInGbp());
    }

    @Test
    public void calculateDividendAmountInGbp_prefersMonthlyWhenItProducesLowerAmount() {
        NonUkDomicileAccumulatingDividend dividend = createAcwiDividend();

        dividend.setMonthlyRate(TaxYearPeriod.MAY, new BigDecimal("0.80"));
        dividend.setYearlyRate(new BigDecimal("0.90"));

        assertBigDecimalEquals("17.60", dividend.calculateDividendAmountInGbpWithMonthlyRate());
        assertBigDecimalEquals("19.80", dividend.calculateDividendAmountInGbpWithYearlyRate());
        assertBigDecimalEquals("17.60", dividend.calculateDividendAmountInGbp());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHoldingMovement_throwsWhenMovementIsBeforeReportStartDate() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addHoldingMovement(LocalDate.of(2024, 11, 30), BigDecimal.ONE, HoldingMovementType.BUY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHoldingMovement_throwsWhenMovementIsAfterReportEndDate() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addHoldingMovement(LocalDate.of(2025, 12, 1), BigDecimal.ONE, HoldingMovementType.BUY);
    }

    @Test(expected = IllegalStateException.class)
    public void calculateDividendAmount_throwsWhenMovementsCreateNegativeHolding() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addHoldingMovement(LocalDate.of(2025, 8, 1), new BigDecimal("11"), HoldingMovementType.SELL);

        dividend.calculateDividendAmount();
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsWhenFundDistributionDateIsOutsideTaxYear() {
        new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2025),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_throwsWhenSecurityIdentifierDoesNotMatchReportIdentifier() {
        Security security = new Security(
                "DIFFERENT",
                "ACWI",
                "iShares MSCI ACWI UCITS ETF",
                CurrencyCode.GBP
        );

        new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                security,
                createAcwiReport(),
                new BigDecimal("10")
        );
    }

    private static NonUkDomicileAccumulatingDividend createAcwiDividend() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );
        dividend.addHoldingMovement(LocalDate.of(2025, 1, 15), new BigDecimal("2"), HoldingMovementType.BUY);
        dividend.addHoldingMovement(LocalDate.of(2025, 8, 1), new BigDecimal("1"), HoldingMovementType.SELL);
        return dividend;
    }

    private static Security createAcwiSecurity() {
        return new Security(
                "IE00B6R52259",
                "ACWI",
                "iShares MSCI ACWI UCITS ETF",
                CurrencyCode.GBP
        );
    }

    private static AccumulatingFundReport createAcwiReport() {
        return new AccumulatingFundReport(
                "IE00B6R52259",
                LocalDate.of(2024, 12, 1),
                LocalDate.of(2025, 11, 30),
                LocalDate.of(2026, 5, 31),
                new BigDecimal("2.00"),
                CurrencyCode.USD
        );
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
