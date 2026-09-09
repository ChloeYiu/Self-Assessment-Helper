package com.helper.ingestion.trade.ibkr;

import com.helper.ingestion.util.table.TabularCell;
import com.helper.ingestion.util.table.TabularDocument;
import com.helper.ingestion.util.table.TabularTable;
import com.helper.ingestion.util.table.TabularTableBuilder;
import com.helper.ingestion.util.api.XmlDocumentParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Extractor for IBKR Flex trade history payloads.
 */
public class IbkrTradeExtractor {
    private static final String TRADES_TABLE_NAME = "Trades";
    private static final String SECURITIES_TABLE_NAME = "Securities";
    private static final List<String> PREFERRED_TRADE_COLUMNS = List.of(
            "accountId",
            "assetCategory",
            "symbol",
            "description",
            "conid",
            "isin",
            "securityID",
            "securityIDType",
            "figi",
            "listingExchange",
            "tradeDate",
            "settleDateTarget",
            "buySell",
            "quantity",
            "tradePrice",
            "proceeds",
            "ibCommission",
            "currency",
            "fifoPnlRealized");
    private static final List<String> SECURITY_COLUMNS = List.of(
            "identifier",
            "identifierType",
            "symbol",
            "description",
            "assetCategory",
            "conid",
            "isin",
            "securityID",
            "securityIDType",
            "figi",
            "listingExchange",
            "currency");

    private final XmlDocumentParser xmlDocumentParser = new XmlDocumentParser();

    public TabularDocument extractFlexStatementDocument(String flexXml) {
        Objects.requireNonNull(flexXml, "flexXml");
        return extractFlexStatementDocument(new ByteArrayInputStream(flexXml.getBytes(StandardCharsets.UTF_8)));
    }

    public TabularDocument extractFlexStatementDocument(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");

        Document document = xmlDocumentParser.parse(inputStream);
        List<Element> tradeRows = findTradeRows(document);
        TabularTable tradesTable = extractTradesTable(tradeRows);
        TabularTable securitiesTable = extractSecuritiesTable(tradeRows);

        return new TabularDocument("IBKR Flex Query", List.of(tradesTable, securitiesTable));
    }

    private TabularTable extractTradesTable(List<Element> tradeRows) {
        List<String> columnNames = findTradeColumnNames(tradeRows);
        TabularTable tradesTable = new TabularTableBuilder<>(
                TRADES_TABLE_NAME,
                columnNames,
                tradeRows,
                this::createTradeCell)
                .buildTable();

        return tradesTable;
    }

    private TabularTable extractSecuritiesTable(List<Element> tradeRows) {
        List<Element> securityRows = findSecurityRows(tradeRows);

        return new TabularTableBuilder<>(
                SECURITIES_TABLE_NAME,
                SECURITY_COLUMNS,
                securityRows,
                this::createSecurityCell)
                .buildTable();
    }

    private List<Element> findTradeRows(Document document) {
        NodeList nodes = document.getElementsByTagName("Trade");
        List<Element> tradeRows = new ArrayList<>();

        for (int index = 0; index < nodes.getLength(); index++) {
            tradeRows.add((Element) nodes.item(index));
        }

        return tradeRows;
    }

    private List<String> findTradeColumnNames(List<Element> tradeRows) {
        Set<String> columnNames = new LinkedHashSet<>();

        for (String preferredColumn : PREFERRED_TRADE_COLUMNS) {
            for (Element tradeRow : tradeRows) {
                if (tradeRow.hasAttribute(preferredColumn)) {
                    columnNames.add(preferredColumn);
                    break;
                }
            }
        }

        for (Element tradeRow : tradeRows) {
            NamedNodeMap attributes = tradeRow.getAttributes();

            for (int index = 0; index < attributes.getLength(); index++) {
                columnNames.add(attributes.item(index).getNodeName());
            }
        }

        return List.copyOf(columnNames);
    }

    private TabularCell createTradeCell(Element tradeRow, String columnName, int rowIndex) {
        return new TabularCell(
                tradeRow.getAttribute(columnName),
                new IbkrFlexSourceLocation(TRADES_TABLE_NAME, rowIndex, columnName));
    }

    private List<Element> findSecurityRows(List<Element> tradeRows) {
        Map<String, Element> securityRowsByIdentifier = new LinkedHashMap<>();

        for (Element tradeRow : tradeRows) {
            if (!isSecurityTradeRow(tradeRow)) {
                continue;
            }

            String identifier = getSecurityIdentifier(tradeRow);

            if (!identifier.isBlank()) {
                securityRowsByIdentifier.putIfAbsent(identifier, tradeRow);
            }
        }

        return List.copyOf(securityRowsByIdentifier.values());
    }

    private boolean isSecurityTradeRow(Element tradeRow) {
        return "STK".equalsIgnoreCase(tradeRow.getAttribute("assetCategory"));
    }

    private TabularCell createSecurityCell(Element tradeRow, String columnName, int rowIndex) {
        return new TabularCell(
                getSecurityCellValue(tradeRow, columnName),
                new IbkrFlexSourceLocation(SECURITIES_TABLE_NAME, rowIndex, columnName));
    }

    private String getSecurityCellValue(Element tradeRow, String columnName) {
        if ("identifier".equals(columnName)) {
            return getSecurityIdentifier(tradeRow);
        }

        if ("identifierType".equals(columnName)) {
            return getSecurityIdentifierType(tradeRow);
        }

        return tradeRow.getAttribute(columnName);
    }

    private String getSecurityIdentifier(Element tradeRow) {
        for (String columnName : List.of("isin", "securityID", "figi", "conid", "symbol")) {
            String value = tradeRow.getAttribute(columnName);

            if (!value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String getSecurityIdentifierType(Element tradeRow) {
        for (String columnName : List.of("isin", "securityID", "figi", "conid", "symbol")) {
            String value = tradeRow.getAttribute(columnName);

            if (!value.isBlank()) {
                return columnName.toUpperCase(Locale.ROOT);
            }
        }

        return "";
    }
}
