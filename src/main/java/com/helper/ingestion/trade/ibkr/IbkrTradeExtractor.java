package com.helper.ingestion.trade.ibkr;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.TradeType;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularRow;
import com.helper.util.table.TabularTable;
import java.util.List;
import java.util.Objects;

/**
 * Extracts domain trades from an IBKR trades table.
 */
public class IbkrTradeExtractor {
    private static final String TRADES_TABLE_NAME = "Trades";

    /** Extracts all trades for one security from a document containing an IBKR Trades table. */
    public List<Trade> extractTrades(TabularDocument document, Security security) {
        Security targetSecurity = Objects.requireNonNull(security, "security");
        TabularTable tradesTable = Objects.requireNonNull(document, "document")
                .getTable(TRADES_TABLE_NAME)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + TRADES_TABLE_NAME + " table"));

        return tradesTable.getRows()
                .stream()
                .filter(row -> targetSecurity.getIdentifier().equals(row.getText("identifier")))
                .map(row -> extractTrade(row, targetSecurity))
                .toList();
    }

    /** Extracts one domain trade from an IBKR trade row. */
    public Trade extractTrade(TabularRow row, Security security) {
        TabularRow tradeRow = Objects.requireNonNull(row, "row");
        return new Trade(
                tradeRow.getText("tradeId"),
                tradeRow.getLocalDate("transactionDate"),
                Objects.requireNonNull(security, "security"),
                tradeRow.getEnum("tradeType", TradeType.class),
                tradeRow.getBigDecimal("quantity"),
                tradeRow.getBigDecimal("grossAmountGbp"),
                tradeRow.getBigDecimal("feeGbp")
        );
    }
}
