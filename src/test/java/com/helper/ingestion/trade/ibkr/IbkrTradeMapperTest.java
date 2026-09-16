package com.helper.ingestion.trade.ibkr;

import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IbkrTradeMapperTest {

        @Test
        public void extractFlexStatementDocument_mapsFlexTradeAttributesToTabularTable() {
                String xml = "<FlexQueryResponse queryName=\"Test Query\" type=\"AF\">"
                                + "<FlexStatements count=\"1\">"
                                + "<FlexStatement>"
                                + "<Trades>"
                                + "<Trade accountId=\"U123\" assetCategory=\"STK\" symbol=\"ABC\" description=\"ABC PLC\" conid=\"12345\""
                                + " isin=\"GB00ABC12345\" securityID=\"GB00ABC12345\" securityIDType=\"ISIN\""
                                + " figi=\"BBG000ABC123\" listingExchange=\"LSE\" tradeDate=\"2025-04-07\""
                                + " buySell=\"BUY\" quantity=\"10\" tradePrice=\"12.34\" proceeds=\"-123.40\""
                                + " ibCommission=\"-1.00\" currency=\"USD\" fifoPnlRealized=\"0\" />"
                                + "<Trade accountId=\"U123\" assetCategory=\"STK\" symbol=\"ABC\" description=\"ABC PLC\" conid=\"12345\""
                                + " isin=\"GB00ABC12345\" securityID=\"GB00ABC12345\" securityIDType=\"ISIN\""
                                + " figi=\"BBG000ABC123\" listingExchange=\"LSE\" tradeDate=\"2025-05-08\""
                                + " buySell=\"SELL\" quantity=\"-5\" tradePrice=\"20.00\" proceeds=\"100.00\""
                                + " ibCommission=\"-1.00\" currency=\"USD\" fifoPnlRealized=\"37.30\" />"
                                + "<Trade accountId=\"U123\" assetCategory=\"CASH\" symbol=\"GBP\" description=\"GBP.USD\" conid=\"0\""
                                + " tradeDate=\"2025-05-08\" buySell=\"BUY\" quantity=\"100\" tradePrice=\"1.25\""
                                + " proceeds=\"-125.00\" currency=\"USD\" />"
                                + "</Trades>"
                                + "</FlexStatement>"
                                + "</FlexStatements>"
                                + "</FlexQueryResponse>";

                TabularDocument document = new IbkrTradeMapper().extractFlexStatementDocument(xml);
                TabularTable tradesTable = document.getTables().get(0);
                TabularTable securitiesTable = document.getTables().get(1);

                assertEquals("IBKR Flex Query", document.getSourceName().orElseThrow());
                assertEquals(2, document.getTables().size());
                assertEquals("Trades", tradesTable.getName().orElseThrow());
                assertEquals(3, tradesTable.getRows().size());
                assertTrue(tradesTable.getColumns().stream()
                                .anyMatch(column -> column.getName().orElseThrow().equals("tradeDate")));

                assertEquals("ABC", tradesTable.getCell(0, "symbol").orElseThrow().getRawText());
                assertEquals("GB00ABC12345", tradesTable.getCell(0, "isin").orElseThrow().getRawText());
                assertEquals("ISIN", tradesTable.getCell(0, "securityIDType").orElseThrow().getRawText());
                assertEquals("LSE", tradesTable.getCell(0, "listingExchange").orElseThrow().getRawText());
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

                assertEquals("Securities", securitiesTable.getName().orElseThrow());
                assertEquals(1, securitiesTable.getRows().size());
                assertEquals("GB00ABC12345", securitiesTable.getCell(0, "identifier").orElseThrow().getRawText());
                assertEquals("ISIN", securitiesTable.getCell(0, "identifierType").orElseThrow().getRawText());
                assertEquals("ABC", securitiesTable.getCell(0, "symbol").orElseThrow().getRawText());
                assertEquals("ABC PLC", securitiesTable.getCell(0, "description").orElseThrow().getRawText());
                assertEquals("STK", securitiesTable.getCell(0, "assetCategory").orElseThrow().getRawText());
                assertEquals("12345", securitiesTable.getCell(0, "conid").orElseThrow().getRawText());
                assertEquals("GB00ABC12345", securitiesTable.getCell(0, "isin").orElseThrow().getRawText());
                assertEquals("BBG000ABC123", securitiesTable.getCell(0, "figi").orElseThrow().getRawText());
                assertEquals("LSE", securitiesTable.getCell(0, "listingExchange").orElseThrow().getRawText());
                assertEquals("USD", securitiesTable.getCell(0, "currency").orElseThrow().getRawText());

                IbkrFlexSourceLocation securitySourceLocation = (IbkrFlexSourceLocation) securitiesTable
                                .getCell(0, "identifier")
                                .orElseThrow()
                                .getSourceLocation()
                                .orElseThrow();
                assertEquals("Securities", securitySourceLocation.getSectionName());
                assertEquals(0, securitySourceLocation.getRowIndex());
                assertEquals("identifier", securitySourceLocation.getFieldName());
        }
}
