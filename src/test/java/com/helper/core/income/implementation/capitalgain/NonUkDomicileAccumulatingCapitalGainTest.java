package com.helper.core.income.implementation.capitalgain;

import com.helper.core.config.CurrencyCode;
import com.helper.core.config.TaxYear;
import com.helper.core.income.artifact.CarryForwardSnapshotArtifact;
import com.helper.core.income.artifact.IncomeArtifactType;
import com.helper.core.income.implementation.capitalgain.model.CapitalGainResult;
import com.helper.core.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.core.income.implementation.capitalgain.model.EriAdjustment;
import com.helper.core.income.implementation.capitalgain.model.Match;
import com.helper.core.income.implementation.capitalgain.model.MatchType;
import com.helper.core.income.implementation.capitalgain.model.Trade;
import com.helper.core.income.implementation.capitalgain.model.TradeBook;
import com.helper.core.income.implementation.capitalgain.model.TradeType;
import com.helper.core.security.Security;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class NonUkDomicileAccumulatingCapitalGainTest {
    private static final TaxYear TAX_YEAR = TaxYear.of(2025);
    private static final LocalDate ERI_DATE = LocalDate.of(2025, 5, 31);
    private static final BigDecimal NO_ERI = BigDecimal.ZERO;
    private static int tradeIdSequence = 1;

    @Test
    public void calculate_matchesSameDayThenThirtyDayThenPoolBeforeLaterBuy() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                tradeBook(
                        sell(LocalDate.of(2026, 2, 20), "5", "700", "5"),
                        buy(LocalDate.of(2026, 2, 20), "2", "300", "0"),
                        buy(LocalDate.of(2026, 3, 5), "2", "240", "0"),
                        buy(LocalDate.of(2026, 3, 25), "3", "330", "0")
                )
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("55.00", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("12", closingPool.getQuantity());
        assertBigDecimalEquals("1230.00", closingPool.getPooledCostGbp());

        assertMatchDetails(
                matches(result).get(0),
                MatchType.SAME_DAY,
                "2",
                "300"
        );
        assertMatchDetails(
                matches(result).get(1),
                MatchType.THIRTY_DAY,
                "2",
                "240"
        );
        assertEquals(2, matches(result).size());
    }

    @Test
    public void calculate_matchesFutureThirtyDayBuyOutsideTaxYearBeforePool() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                tradeBook(
                        sell(LocalDate.of(2026, 3, 27), "5", "700", "5"),
                        buy(LocalDate.of(2026, 4, 10), "2", "240", "0")
                )
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("155.00", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("7", closingPool.getQuantity());
        assertBigDecimalEquals("700.00", closingPool.getPooledCostGbp());

        assertEquals(1, matches(result).size());
        assertMatchDetails(
                matches(result).get(0),
                MatchType.THIRTY_DAY,
                "2",
                "240"
        );
    }

    @Test
    public void calculate_addsEarlierBuyToPoolBeforeLaterSell() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                tradeBook(
                        buy(LocalDate.of(2026, 3, 10), "2", "240", "0"),
                        sell(LocalDate.of(2026, 3, 20), "5", "700", "5")
                )
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("178.33", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("7", closingPool.getQuantity());
        assertBigDecimalEquals("723.33", closingPool.getPooledCostGbp());
        assertEquals(0, matches(result).size());
    }

    @Test
    public void calculate_doesNotThirtyDayMatchBuyBeforeSell() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                tradeBook(
                        buy(LocalDate.of(2026, 3, 10), "2", "240", "0"),
                        sell(LocalDate.of(2026, 3, 20), "2", "300", "0")
                )
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("93.33", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("10", closingPool.getQuantity());
        assertBigDecimalEquals("1033.33", closingPool.getPooledCostGbp());
        assertEquals(0, matches(result).size());
    }

    @Test
    public void calculate_sortsTradesByTransactionDateBeforePooling() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                tradeBook(
                        sell(LocalDate.of(2026, 3, 20), "5", "700", "5"),
                        buy(LocalDate.of(2026, 3, 10), "2", "240", "0")
                )
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("178.33", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("7", closingPool.getQuantity());
        assertBigDecimalEquals("723.33", closingPool.getPooledCostGbp());
    }

    @Test
    public void calculate_appliesEriBeforeSameDayPoolSell() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                "50",
                tradeBook(sell(ERI_DATE, "5", "700", "5"))
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("170.00", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("5", closingPool.getQuantity());
        assertBigDecimalEquals("525.00", closingPool.getPooledCostGbp());
    }

    @Test
    public void calculate_canReturnCapitalLoss() {
        NonUkDomicileAccumulatingCapitalGain capitalGain = capitalGain(
                "10",
                "1000",
                tradeBook(sell(LocalDate.of(2026, 3, 20), "5", "400", "5"))
        );

        CapitalGainResult result = capitalGain.calculateCapitalGain();
        assertBigDecimalEquals("-105.00", result.getCapitalGainAmount());

        CarryForwardSnapshot closingPool = closingSnapshot(result);
        assertBigDecimalEquals("5", closingPool.getQuantity());
        assertBigDecimalEquals("500.00", closingPool.getPooledCostGbp());
    }

    @Test
    public void constructor_rejectsOpeningSnapshotForDifferentSecurity() {
        Security security = createSecurity();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(TAX_YEAR, createOtherSecurity(), new BigDecimal("10"), new BigDecimal("1000")),
                        new EriAdjustment(ERI_DATE, security, NO_ERI),
                        tradeBook()
                )
        );
    }

    @Test
    public void constructor_rejectsEriForDifferentSecurity() {
        Security security = createSecurity();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(TAX_YEAR, security, new BigDecimal("10"), new BigDecimal("1000")),
                        new EriAdjustment(ERI_DATE, createOtherSecurity(), NO_ERI),
                        tradeBook()
                )
        );
    }

    @Test
    public void constructor_rejectsEriOutsideTaxYear() {
        Security security = createSecurity();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(TAX_YEAR, security, new BigDecimal("10"), new BigDecimal("1000")),
                        new EriAdjustment(LocalDate.of(2026, 4, 6), security, NO_ERI),
                        tradeBook()
                )
        );
    }

    @Test
    public void constructor_rejectsTradeBookForDifferentSecurity() {
        Security security = createSecurity();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(TAX_YEAR, security, new BigDecimal("10"), new BigDecimal("1000")),
                        new EriAdjustment(ERI_DATE, security, NO_ERI),
                        new TradeBook(createOtherSecurity(), TAX_YEAR)
                )
        );
    }

    @Test
    public void constructor_rejectsTradeBookForDifferentTaxYear() {
        Security security = createSecurity();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(TAX_YEAR, security, new BigDecimal("10"), new BigDecimal("1000")),
                        new EriAdjustment(ERI_DATE, security, NO_ERI),
                        new TradeBook(security, TaxYear.of(2024))
                )
        );
    }

    @Test
    public void constructor_rejectsConsumedAcquisitionMissingFromTradeBook() {
        Security security = createSecurity();

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(
                                TAX_YEAR,
                                security,
                                new BigDecimal("10"),
                                new BigDecimal("1000"),
                                List.of(new Match("BUY_MISSING", "SELL_1", new BigDecimal("1"), new BigDecimal("100"), MatchType.THIRTY_DAY))
                        ),
                        new EriAdjustment(ERI_DATE, security, NO_ERI),
                        tradeBook()
                )
        );
    }

    @Test
    public void constructor_rejectsConsumedAcquisitionAboveAvailableBuyQuantity() {
        Security security = createSecurity();
        Trade availableBuy = new Trade(
                "BUY_CARRY_FORWARD",
                LocalDate.of(2026, 3, 20),
                security,
                TradeType.BUY,
                new BigDecimal("1"),
                new BigDecimal("100"),
                BigDecimal.ZERO
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new NonUkDomicileAccumulatingCapitalGain(
                        TAX_YEAR,
                        security,
                        new CarryForwardSnapshot(
                                TAX_YEAR,
                                security,
                                new BigDecimal("10"),
                                new BigDecimal("1000"),
                                List.of(new Match("BUY_CARRY_FORWARD", "SELL_1", new BigDecimal("2"), new BigDecimal("100"), MatchType.THIRTY_DAY))
                        ),
                        new EriAdjustment(ERI_DATE, security, NO_ERI),
                        tradeBook(availableBuy)
                )
        );
    }

    private static NonUkDomicileAccumulatingCapitalGain capitalGain(
            String openingQuantity,
            String openingCostGbp,
            TradeBook tradeBook
    ) {
        return capitalGain(openingQuantity, openingCostGbp, NO_ERI, tradeBook);
    }

    private static NonUkDomicileAccumulatingCapitalGain capitalGain(
            String openingQuantity,
            String openingCostGbp,
            String eriAmountGbp,
            TradeBook tradeBook
    ) {
        return capitalGain(openingQuantity, openingCostGbp, new BigDecimal(eriAmountGbp), tradeBook);
    }

    private static NonUkDomicileAccumulatingCapitalGain capitalGain(
            String openingQuantity,
            String openingCostGbp,
            BigDecimal eriAmountGbp,
            TradeBook tradeBook
    ) {
        Security security = createSecurity();
        return new NonUkDomicileAccumulatingCapitalGain(
                TAX_YEAR,
                security,
                new CarryForwardSnapshot(
                        TAX_YEAR,
                        security,
                        new BigDecimal(openingQuantity),
                        new BigDecimal(openingCostGbp)
                ),
                new EriAdjustment(
                        ERI_DATE,
                        security,
                        eriAmountGbp
                ),
                tradeBook
        );
    }

    private static TradeBook tradeBook(Trade... trades) {
        TradeBook tradeBook = new TradeBook(
                createSecurity(),
                TAX_YEAR
        );
        for (Trade trade : trades) {
            tradeBook.add(trade);
        }
        return tradeBook;
    }

    private static Trade buy(
            LocalDate transactionDate,
            String quantity,
            String grossAmountGbp,
            String feeGbp
    ) {
        return new Trade(
                TradeType.BUY.name() + "_" + tradeIdSequence++,
                transactionDate,
                createSecurity(),
                TradeType.BUY,
                new BigDecimal(quantity),
                new BigDecimal(grossAmountGbp),
                new BigDecimal(feeGbp)
        );
    }

    private static Trade sell(
            LocalDate transactionDate,
            String quantity,
            String grossAmountGbp,
            String feeGbp
    ) {
        return new Trade(
                TradeType.SELL.name() + "_" + tradeIdSequence++,
                transactionDate,
                createSecurity(),
                TradeType.SELL,
                new BigDecimal(quantity),
                new BigDecimal(grossAmountGbp),
                new BigDecimal(feeGbp)
        );
    }

    private static Security createSecurity() {
        return createSecurity("IE00B6R52259");
    }

    private static Security createOtherSecurity() {
        return createSecurity("IE00B53SZB19");
    }

    private static Security createSecurity(String identifier) {
        return new Security(
                identifier,
                "ACWI",
                "iShares MSCI ACWI UCITS ETF",
                CurrencyCode.GBP
        );
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    private static CarryForwardSnapshot closingSnapshot(CapitalGainResult result) {
        return result.getArtifacts()
                .stream()
                .filter(artifact -> artifact.getArtifactType() == IncomeArtifactType.CAPITAL_GAIN_CARRY_FORWARD_SNAPSHOT)
                .map(CarryForwardSnapshotArtifact.class::cast)
                .map(CarryForwardSnapshotArtifact::getSnapshot)
                .findFirst()
                .orElseThrow();
    }

    private static List<Match> matches(CapitalGainResult result) {
        return closingSnapshot(result).getConsumedAcquisitions();
    }

    private static void assertMatchDetails(
            Match actual,
            MatchType matchType,
            String quantity,
            String costGbp
    ) {
        assertEquals(matchType, actual.getMatchType());
        assertBigDecimalEquals(quantity, actual.getQuantity());
        assertBigDecimalEquals(costGbp, actual.getCostGbp());
    }
}
