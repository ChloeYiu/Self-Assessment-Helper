package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;

import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import com.helper.util.table.TabularDocument;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.Test;

public class IbkrTradeExtractorTest {
    @Test
    public void extractTrade_mapsTradeRowToDomainTrade() {
        Trade trade = new IbkrTradeExtractor().extractTrade(
                IbkrTestFixtures.acwiBuyTradesTable().getRows().get(0),
                IbkrTestFixtures.ACWI
        );

        assertEquals("BUY_1", trade.getTradeId());
        assertEquals(LocalDate.of(2025, 4, 7), trade.getTransactionDate());
        assertEquals("IE00B44Z5B48", trade.getSecurity().getIdentifier());
        assertEquals(TradeType.BUY, trade.getTradeType());
        assertBigDecimalEquals("10", trade.getQuantity());
        assertBigDecimalEquals("100.25", trade.getGrossAmountGbp());
        assertBigDecimalEquals("1.25", trade.getFeeGbp());
    }

    @Test
    public void extractTrades_returnsOnlyTradesForRequestedSecurity() {
        TabularDocument document = IbkrTestFixtures.acwiBuyAndOtherSellDocument();

        List<Trade> trades = new IbkrTradeExtractor().extractTrades(document, IbkrTestFixtures.ACWI);

        assertEquals(1, trades.size());
        assertEquals("BUY_1", trades.get(0).getTradeId());
        assertEquals(TradeType.BUY, trades.get(0).getTradeType());
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
