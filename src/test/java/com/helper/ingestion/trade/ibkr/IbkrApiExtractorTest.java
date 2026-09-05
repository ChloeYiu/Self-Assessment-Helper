package com.helper.ingestion.trade.ibkr;

import com.helper.ingestion.util.table.TabularDocument;
import com.helper.ingestion.util.table.TabularTable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IbkrApiExtractorTest {

    @Test
    public void extractTradeTable_mapsFlexTradeAttributesToTabularTable() {
        String xml = "<FlexQueryResponse queryName=\"Test Query\" type=\"AF\">"
                + "<FlexStatements count=\"1\">"
                + "<FlexStatement>"
                + "<Trades>"
                + "<Trade accountId=\"U123\" assetCategory=\"STK\" symbol=\"ABC\" tradeDate=\"2025-04-07\""
                + " buySell=\"BUY\" quantity=\"10\" tradePrice=\"12.34\" proceeds=\"-123.40\""
                + " ibCommission=\"-1.00\" currency=\"USD\" fifoPnlRealized=\"0\" />"
                + "<Trade accountId=\"U123\" assetCategory=\"STK\" symbol=\"ABC\" tradeDate=\"2025-05-08\""
                + " buySell=\"SELL\" quantity=\"-5\" tradePrice=\"20.00\" proceeds=\"100.00\""
                + " ibCommission=\"-1.00\" currency=\"USD\" fifoPnlRealized=\"37.30\" />"
                + "</Trades>"
                + "</FlexStatement>"
                + "</FlexStatements>"
                + "</FlexQueryResponse>";

        TabularDocument document = new IbkrApiExtractor().extractTradeTable(xml);
        TabularTable tradesTable = document.getTables().get(0);

        assertEquals("IBKR Flex Query", document.getSourceName().orElseThrow());
        assertEquals("Trades", tradesTable.getName().orElseThrow());
        assertEquals(2, tradesTable.getRows().size());
        assertTrue(tradesTable.getColumns().stream()
                .anyMatch(column -> column.getName().orElseThrow().equals("tradeDate")));

        assertEquals("ABC", tradesTable.getCell(0, "symbol").orElseThrow().getRawText());
        assertEquals("BUY", tradesTable.getCell(0, "buySell").orElseThrow().getRawText());
        assertEquals("-123.40", tradesTable.getCell(0, "proceeds").orElseThrow().getRawText());
        assertEquals("SELL", tradesTable.getCell(1, "buySell").orElseThrow().getRawText());
        assertEquals("37.30", tradesTable.getCell(1, "fifoPnlRealized").orElseThrow().getRawText());

        IbkrFlexSourceLocation sourceLocation = (IbkrFlexSourceLocation) tradesTable
                .getCell(1, "fifoPnlRealized")
                .orElseThrow()
                .getSourceLocation()
                .orElseThrow();
        assertEquals("Trades", sourceLocation.getSectionName());
        assertEquals(1, sourceLocation.getRowIndex());
        assertEquals("fifoPnlRealized", sourceLocation.getFieldName());
    }
}
