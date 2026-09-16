package com.helper.ingestion.trade.ibkr;

import com.helper.config.TaxYear;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.capitalgain.model.TradeBook;
import com.helper.income.implementation.TradeType;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularRow;
import com.helper.util.table.TabularTable;
import java.util.Objects;

/**
 * Extracts a capital-gain trade book from an IBKR Flex trades table.
 */
public class IbkrTradeBookExtractor {
    private static final String TRADES_TABLE_NAME = "Trades";

    /** Extracts a trade book for one security and tax year. */
    public TradeBook extractTradeBook(TabularDocument document, Security security, TaxYear taxYear) {
        Security targetSecurity = Objects.requireNonNull(security, "security");
        TradeBook tradeBook = new TradeBook(targetSecurity, Objects.requireNonNull(taxYear, "taxYear"));
        TabularTable tradesTable = Objects.requireNonNull(document, "document")
                .getTable(TRADES_TABLE_NAME)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + TRADES_TABLE_NAME + " table"));

        tradesTable.getRows()
                .stream()
                .filter(row -> targetSecurity.getIdentifier().equals(row.getText("identifier")))
                .map(row -> extractTrade(row, targetSecurity))
                .forEach(tradeBook::add);

        return tradeBook;
    }

    private Trade extractTrade(TabularRow row, Security security) {
        return new Trade(
                row.getText("tradeId"),
                row.getLocalDate("transactionDate"),
                security,
                row.getEnum("tradeType", TradeType.class),
                row.getBigDecimal("quantity"),
                row.getBigDecimal("grossAmountGbp"),
                row.getBigDecimal("feeGbp")
        );
    }

}
