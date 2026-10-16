package com.helper.ingestion.trade.ibkr;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularRow;
import com.helper.util.table.TabularTable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Extracts domain trades from an IBKR trades table.
 */
public class IbkrTradeExtractor {
    private static final DateTimeFormatter IBKR_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    /** Extracts all trades for one security from a document containing an IBKR Trades table. */
    public List<Trade> extractTrades(TabularDocument document, Security security) {
        Security targetSecurity = Objects.requireNonNull(security, "security");
        TabularTable tradesTable = Objects.requireNonNull(document, "document")
                .getTable(IbkrNames.Tables.TRADES)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + IbkrNames.Tables.TRADES + " table"));

        return tradesTable.getRows()
                .stream()
                .filter(row -> targetSecurity.getIdentifier().equals(row.getText(IbkrNames.RawTradeColumns.ISIN)))
                .map(row -> extractTrade(row, targetSecurity))
                .toList();
    }

    /** Extracts one domain trade from an IBKR trade row. */
    public Trade extractTrade(TabularRow row, Security security) {
        TabularRow tradeRow = Objects.requireNonNull(row, "row");
        requireGbpCurrency(tradeRow);
        return new Trade(
                tradeRow.getText(IbkrNames.RawTradeColumns.TRANSACTION_ID),
                java.time.LocalDate.parse(
                        tradeRow.getText(IbkrNames.RawTradeColumns.TRADE_DATE),
                        IBKR_DATE_FORMATTER),
                Objects.requireNonNull(security, "security"),
                tradeRow.getEnum(IbkrNames.RawTradeColumns.BUY_SELL, TradeType.class),
                tradeRow.getBigDecimal(IbkrNames.RawTradeColumns.QUANTITY).abs(),
                tradeRow.getBigDecimal(IbkrNames.RawTradeColumns.PROCEEDS).abs(),
                tradeRow.getBigDecimal(IbkrNames.RawTradeColumns.IB_COMMISSION).abs()
        );
    }

    private static void requireGbpCurrency(TabularRow row) {
        String currency = row.getText(IbkrNames.RawTradeColumns.CURRENCY);
        if (!"GBP".equals(currency)) {
            throw new IllegalArgumentException("IBKR trade currency must be GBP until FX normalization is implemented: " + currency);
        }
    }
}
