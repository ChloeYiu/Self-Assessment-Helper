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
    /** Extracts all trades for one security from a document containing an IBKR Trades table. */
    public List<Trade> extractTrades(TabularDocument document, Security security) {
        Security targetSecurity = Objects.requireNonNull(security, "security");
        TabularTable tradesTable = Objects.requireNonNull(document, "document")
                .getTable(IbkrNames.Tables.TRADES)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + IbkrNames.Tables.TRADES + " table"));

        return tradesTable.getRows()
                .stream()
                .filter(row -> targetSecurity.getIdentifier().equals(row.getText(IbkrNames.NormalizedTradeColumns.IDENTIFIER)))
                .map(row -> extractTrade(row, targetSecurity))
                .toList();
    }

    /** Extracts one domain trade from an IBKR trade row. */
    public Trade extractTrade(TabularRow row, Security security) {
        TabularRow tradeRow = Objects.requireNonNull(row, "row");
        return new Trade(
                tradeRow.getText(IbkrNames.NormalizedTradeColumns.TRADE_ID),
                tradeRow.getLocalDate(IbkrNames.NormalizedTradeColumns.TRANSACTION_DATE),
                Objects.requireNonNull(security, "security"),
                tradeRow.getEnum(IbkrNames.NormalizedTradeColumns.TRADE_TYPE, TradeType.class),
                tradeRow.getBigDecimal(IbkrNames.NormalizedTradeColumns.QUANTITY),
                tradeRow.getBigDecimal(IbkrNames.NormalizedTradeColumns.GROSS_AMOUNT_GBP),
                tradeRow.getBigDecimal(IbkrNames.NormalizedTradeColumns.FEE_GBP)
        );
    }
}
