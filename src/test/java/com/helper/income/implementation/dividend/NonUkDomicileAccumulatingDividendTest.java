package com.helper.income.implementation.dividend;

import com.helper.config.CurrencyCode;
import com.helper.config.TaxYear;
import com.helper.config.TaxYearPeriod;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.income.implementation.dividend.model.DividendResult;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class NonUkDomicileAccumulatingDividendTest {

    @Test
    public void calculateDividendResult_usesTradesToCalculateHoldingAtReportingPeriodEnd() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addTrades(List.of(
                createTrade(LocalDate.of(2025, 1, 15), new BigDecimal("2"), TradeType.BUY),
                createTrade(LocalDate.of(2025, 8, 1), new BigDecimal("1"), TradeType.SELL)
        ));
        dividend.setMonthlyRates(Map.of(TaxYearPeriod.MAY, new BigDecimal("0.90")));

        DividendResult result = dividend.calculateDividendResult();

        assertBigDecimalEquals("19.80", result.getDividendAmount());
        assertEquals(1, result.getArtifacts().size());
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

        dividend.setMonthlyRates(Map.of(TaxYearPeriod.MAY, new BigDecimal("0.90")));
        dividend.setYearlyRate(new BigDecimal("0.80"));

        DividendResult result = dividend.calculateDividendResult();

        assertBigDecimalEquals("17.60", result.getDividendAmount());
    }

    @Test
    public void calculateDividendAmountInGbp_prefersMonthlyWhenItProducesLowerAmount() {
        NonUkDomicileAccumulatingDividend dividend = createAcwiDividend();

        dividend.setMonthlyRates(Map.of(TaxYearPeriod.MAY, new BigDecimal("0.80")));
        dividend.setYearlyRate(new BigDecimal("0.90"));

        DividendResult result = dividend.calculateDividendResult();

        assertBigDecimalEquals("17.60", result.getDividendAmount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTrade_throwsWhenTradeIsBeforeReportStartDate() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addTrade(createTrade(LocalDate.of(2024, 11, 30), BigDecimal.ONE, TradeType.BUY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTrade_throwsWhenTradeIsAfterReportEndDate() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addTrade(createTrade(LocalDate.of(2025, 12, 1), BigDecimal.ONE, TradeType.BUY));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addTrade_throwsWhenTradeSecurityDoesNotMatchDividendSecurity() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addTrade(new Trade(
                "BUY_DIFFERENT_SECURITY",
                LocalDate.of(2025, 1, 15),
                new Security(
                        "DIFFERENT",
                        "DIFF",
                        "Different security",
                        CurrencyCode.GBP
                ),
                TradeType.BUY,
                BigDecimal.ONE,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        ));
    }

    @Test(expected = IllegalStateException.class)
    public void calculateDividendResult_throwsWhenTradesCreateNegativeHolding() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );

        dividend.addTrade(createTrade(LocalDate.of(2025, 8, 1), new BigDecimal("11"), TradeType.SELL));

        dividend.calculateDividendResult();
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

    private static Trade createTrade(LocalDate transactionDate, BigDecimal quantity, TradeType tradeType) {
        return new Trade(
                tradeType.name() + "_" + transactionDate + "_" + quantity,
                transactionDate,
                createAcwiSecurity(),
                tradeType,
                quantity,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    private static NonUkDomicileAccumulatingDividend createAcwiDividend() {
        NonUkDomicileAccumulatingDividend dividend = new NonUkDomicileAccumulatingDividend(
                TaxYear.of(2026),
                createAcwiSecurity(),
                createAcwiReport(),
                new BigDecimal("10")
        );
        dividend.addTrades(List.of(
                createTrade(LocalDate.of(2025, 1, 15), new BigDecimal("2"), TradeType.BUY),
                createTrade(LocalDate.of(2025, 8, 1), new BigDecimal("1"), TradeType.SELL)
        ));
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
