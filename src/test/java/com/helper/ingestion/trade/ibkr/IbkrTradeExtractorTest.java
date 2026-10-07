package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularTableBuilder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class IbkrTradeExtractorTest {
    private static final Security ACWI = new Security(
            "IE00B44Z5B48",
            "ACWI",
            "SPDR MSCI ACWI UCITS ETF",
            CurrencyCode.USD
    );

    @Test
    public void extractTrade_mapsTradeRowToDomainTrade() {
        Trade trade = new IbkrTradeExtractor().extractTrade(
                createTradeTable(List.of(Map.of(
                        "identifier", "IE00B44Z5B48",
                        "tradeId", "BUY_1",
                        "transactionDate", "2025-04-07",
                        "tradeType", "BUY",
                        "quantity", "10",
                        "grossAmountGbp", "100.25",
                        "feeGbp", "1.25"
                ))).getRows().get(0),
                ACWI
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
        TabularDocument document = new TabularDocument(List.of(createTradeTable(List.of(
                Map.of(
                        "identifier", "IE00B44Z5B48",
                        "tradeId", "BUY_1",
                        "transactionDate", "2025-04-07",
                        "tradeType", "BUY",
                        "quantity", "10",
                        "grossAmountGbp", "100.25",
                        "feeGbp", "1.25"
                ),
                Map.of(
                        "identifier", "DIFFERENT",
                        "tradeId", "SELL_1",
                        "transactionDate", "2025-04-08",
                        "tradeType", "SELL",
                        "quantity", "5",
                        "grossAmountGbp", "60.00",
                        "feeGbp", "1.00"
                )
        ))));

        List<Trade> trades = new IbkrTradeExtractor().extractTrades(document, ACWI);

        assertEquals(1, trades.size());
        assertEquals("BUY_1", trades.get(0).getTradeId());
        assertEquals(TradeType.BUY, trades.get(0).getTradeType());
    }

    private static TabularTable createTradeTable(List<Map<String, String>> rows) {
        return new TabularTableBuilder<>(
                "Trades",
                List.of("identifier", "tradeId", "transactionDate", "tradeType", "quantity", "grossAmountGbp", "feeGbp"),
                rows,
                (row, columnName, rowIndex) -> new TabularCell(row.get(columnName))
        ).buildTable();
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
