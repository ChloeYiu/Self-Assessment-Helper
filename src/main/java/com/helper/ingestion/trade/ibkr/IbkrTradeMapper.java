package com.helper.ingestion.trade.ibkr;

import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularTableBuilder;
import com.helper.util.api.XmlDocumentParser;
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
 * Maps IBKR Flex trade history payloads into tabular documents.
 */
public class IbkrTradeMapper {
    private static final List<String> PREFERRED_TRADE_COLUMNS = List.of(
            IbkrNames.RawTradeColumns.ACCOUNT_ID,
            IbkrNames.RawTradeColumns.ASSET_CATEGORY,
            IbkrNames.RawTradeColumns.SYMBOL,
            IbkrNames.RawTradeColumns.DESCRIPTION,
            IbkrNames.RawTradeColumns.CONID,
            IbkrNames.RawTradeColumns.ISIN,
            IbkrNames.RawTradeColumns.SECURITY_ID,
            IbkrNames.RawTradeColumns.SECURITY_ID_TYPE,
            IbkrNames.RawTradeColumns.FIGI,
            IbkrNames.RawTradeColumns.LISTING_EXCHANGE,
            IbkrNames.RawTradeColumns.TRADE_DATE,
            IbkrNames.RawTradeColumns.SETTLE_DATE_TARGET,
            IbkrNames.RawTradeColumns.BUY_SELL,
            IbkrNames.RawTradeColumns.QUANTITY,
            IbkrNames.RawTradeColumns.TRADE_PRICE,
            IbkrNames.RawTradeColumns.PROCEEDS,
            IbkrNames.RawTradeColumns.IB_COMMISSION,
            IbkrNames.RawTradeColumns.CURRENCY,
            IbkrNames.RawTradeColumns.FIFO_PNL_REALIZED);
    private static final List<String> SECURITY_COLUMNS = List.of(
            IbkrNames.SecurityColumns.IDENTIFIER,
            IbkrNames.SecurityColumns.IDENTIFIER_TYPE,
            IbkrNames.SecurityColumns.SYMBOL,
            IbkrNames.SecurityColumns.DESCRIPTION,
            IbkrNames.SecurityColumns.ASSET_CATEGORY,
            IbkrNames.SecurityColumns.CONID,
            IbkrNames.SecurityColumns.ISIN,
            IbkrNames.SecurityColumns.SECURITY_ID,
            IbkrNames.SecurityColumns.SECURITY_ID_TYPE,
            IbkrNames.SecurityColumns.FIGI,
            IbkrNames.SecurityColumns.LISTING_EXCHANGE,
            IbkrNames.SecurityColumns.CURRENCY);

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
                IbkrNames.Tables.TRADES,
                columnNames,
                tradeRows,
                this::createTradeCell)
                .buildTable();

        return tradesTable;
    }

    private TabularTable extractSecuritiesTable(List<Element> tradeRows) {
        List<Element> securityRows = findSecurityRows(tradeRows);

        return new TabularTableBuilder<>(
                IbkrNames.Tables.SECURITIES,
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
                new IbkrFlexSourceLocation(IbkrNames.Tables.TRADES, rowIndex, columnName));
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
        return "STK".equalsIgnoreCase(tradeRow.getAttribute(IbkrNames.RawTradeColumns.ASSET_CATEGORY));
    }

    private TabularCell createSecurityCell(Element tradeRow, String columnName, int rowIndex) {
        return new TabularCell(
                getSecurityCellValue(tradeRow, columnName),
                new IbkrFlexSourceLocation(IbkrNames.Tables.SECURITIES, rowIndex, columnName));
    }

    private String getSecurityCellValue(Element tradeRow, String columnName) {
        if (IbkrNames.SecurityColumns.IDENTIFIER.equals(columnName)) {
            return getSecurityIdentifier(tradeRow);
        }

        if (IbkrNames.SecurityColumns.IDENTIFIER_TYPE.equals(columnName)) {
            return getSecurityIdentifierType(tradeRow);
        }

        return tradeRow.getAttribute(columnName);
    }

    private String getSecurityIdentifier(Element tradeRow) {
        for (String columnName : List.of(IbkrNames.RawTradeColumns.ISIN, IbkrNames.RawTradeColumns.SECURITY_ID, IbkrNames.RawTradeColumns.FIGI, IbkrNames.RawTradeColumns.CONID, IbkrNames.RawTradeColumns.SYMBOL)) {
            String value = tradeRow.getAttribute(columnName);

            if (!value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String getSecurityIdentifierType(Element tradeRow) {
        for (String columnName : List.of(IbkrNames.RawTradeColumns.ISIN, IbkrNames.RawTradeColumns.SECURITY_ID, IbkrNames.RawTradeColumns.FIGI, IbkrNames.RawTradeColumns.CONID, IbkrNames.RawTradeColumns.SYMBOL)) {
            String value = tradeRow.getAttribute(columnName);

            if (!value.isBlank()) {
                return columnName.toUpperCase(Locale.ROOT);
            }
        }

        return "";
    }
}
