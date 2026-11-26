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
            "isin", "IE00B44Z5B48",
            "transactionID", "BUY_1",
            "tradeDate", "20250407",
            "buySell", "BUY",
            "quantity", "10",
            "proceeds", "-100.25",
            "ibCommission", "-1.25",
            "currency", "GBP");
    private static final Map<String, String> OTHER_SELL = Map.of(
            "isin", "DIFFERENT",
            "transactionID", "SELL_1",
            "tradeDate", "20250408",
            "buySell", "SELL",
            "quantity", "-5",
            "proceeds", "60.00",
            "ibCommission", "-1.00",
            "currency", "GBP");
    private static final Map<String, String> CASH_BUY = Map.of(
            "isin", "",
            "transactionID", "CASH_1",
            "tradeDate", "20250407",
            "buySell", "BUY",
            "quantity", "100",
            "proceeds", "-100.00",
            "ibCommission", "0",
            "currency", "GBP");

    private IbkrTestFixtures() {
    }

    public static TabularDocument acwiBuyDocument() {
        return new TabularDocument(List.of(acwiSecuritiesTable(), acwiBuyTradesTable()));
    }

    public static TabularDocument acwiBuyWithOpenPositionsDocument() {
        return new TabularDocument(List.of(
                acwiSecuritiesTable(), acwiBuyTradesTable(), acwiOpenPositionsTable()));
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
                List.of("isin", "transactionID", "tradeDate", "buySell", "quantity", "proceeds", "ibCommission", "currency"),
                List.of(ACWI_BUY),
                (row, column, index) -> new TabularCell(row.get(column)))
                .buildTable();
    }

    public static TabularDocument acwiBuyWithOtherSecurityAndCashDocument() {
        return new TabularDocument(List.of(new TabularTableBuilder<>(
                "Trades",
                List.of("isin", "transactionID", "tradeDate", "buySell", "quantity", "proceeds", "ibCommission", "currency"),
                List.of(ACWI_BUY, OTHER_SELL, CASH_BUY),
                (row, column, index) -> new TabularCell(row.get(column)))
                .buildTable()));
    }

    public static TabularDocument acwiOpenPositionsDocument() {
        return new TabularDocument(List.of(acwiOpenPositionsTable()));
    }

    private static TabularTable acwiOpenPositionsTable() {
        return new TabularTableBuilder<>(
                IbkrNames.Tables.OPEN_POSITIONS,
                List.of(
                        IbkrNames.OpenPositionColumns.ACCOUNT_ID,
                        IbkrNames.OpenPositionColumns.ISIN,
                        IbkrNames.OpenPositionColumns.SYMBOL,
                        IbkrNames.OpenPositionColumns.POSITION,
                        IbkrNames.OpenPositionColumns.REPORT_DATE),
                List.of(
                        Map.of(
                                "accountId", "U123",
                                "isin", "IE00B44Z5B48",
                                "symbol", "ACWI",
                                "position", "10",
                                "reportDate", "2025-10-06"),
                        Map.of(
                                "accountId", "U123",
                                "isin", "",
                                "symbol", "GBP",
                                "position", "100",
                                "reportDate", "2025-10-06")),
                (row, column, index) -> new TabularCell(row.get(column)))
                .buildTable();
    }
}
