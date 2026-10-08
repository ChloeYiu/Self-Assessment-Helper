package com.helper.ingestion.trade.ibkr;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.Security;
import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularTableBuilder;
import java.util.List;
import java.util.Map;

/** Shared IBKR test data for tests that need ACWI source facts. */
public final class IbkrTestFixtures {
    public static final Security ACWI = new Security(
            "IE00B44Z5B48",
            "ACWI",
            "SPDR MSCI ACWI UCITS ETF",
            CurrencyCode.USD);
    private static final Map<String, String> ACWI_BUY = Map.of(
            "identifier", "IE00B44Z5B48",
            "tradeId", "BUY_1",
            "transactionDate", "2025-04-07",
            "tradeType", "BUY",
            "quantity", "10",
            "grossAmountGbp", "100.25",
            "feeGbp", "1.25");
    private static final Map<String, String> OTHER_SELL = Map.of(
            "identifier", "DIFFERENT",
            "tradeId", "SELL_1",
            "transactionDate", "2025-04-08",
            "tradeType", "SELL",
            "quantity", "5",
            "grossAmountGbp", "60.00",
            "feeGbp", "1.00");

    private IbkrTestFixtures() {
    }

    public static TabularDocument acwiBuyDocument() {
        return new TabularDocument(List.of(acwiSecuritiesTable(), acwiBuyTradesTable()));
    }

    public static TabularTable acwiSecuritiesTable() {
        return new TabularTableBuilder<>(
                "Securities",
                List.of("identifier", "symbol", "description", "currency"),
                List.of(Map.of(
                        "identifier", "IE00B44Z5B48",
                        "symbol", "ACWI",
                        "description", "SPDR MSCI ACWI UCITS ETF",
                        "currency", "USD")),
                (row, column, index) -> new TabularCell(row.get(column)))
                .buildTable();
    }

    public static TabularTable acwiBuyTradesTable() {
        return new TabularTableBuilder<>(
                "Trades",
                List.of("identifier", "tradeId", "transactionDate", "tradeType", "quantity", "grossAmountGbp", "feeGbp"),
                List.of(ACWI_BUY),
                (row, column, index) -> new TabularCell(row.get(column)))
                .buildTable();
    }

    public static TabularDocument acwiBuyAndOtherSellDocument() {
        return new TabularDocument(List.of(new TabularTableBuilder<>(
                "Trades",
                List.of("identifier", "tradeId", "transactionDate", "tradeType", "quantity", "grossAmountGbp", "feeGbp"),
                List.of(ACWI_BUY, OTHER_SELL),
                (row, column, index) -> new TabularCell(row.get(column)))
                .buildTable()));
    }
}
