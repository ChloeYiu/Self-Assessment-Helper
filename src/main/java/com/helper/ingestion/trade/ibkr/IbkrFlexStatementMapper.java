package com.helper.ingestion.trade.ibkr;

import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularTableBuilder;
import com.helper.util.api.XmlDocumentParser;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
 * Maps IBKR Flex Activity and Positions payloads into tabular documents.
 */
public class IbkrFlexStatementMapper {
    private static final DateTimeFormatter FLEX_DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
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
            IbkrNames.RawTradeColumns.TRANSACTION_ID,
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
    private static final List<String> OPEN_POSITION_COLUMNS = List.of(
            IbkrNames.OpenPositionColumns.ACCOUNT_ID,
            IbkrNames.OpenPositionColumns.ASSET_CATEGORY,
            IbkrNames.OpenPositionColumns.ISIN,
            IbkrNames.OpenPositionColumns.POSITION,
            IbkrNames.OpenPositionColumns.REPORT_DATE,
            IbkrNames.OpenPositionColumns.SYMBOL);

    private final XmlDocumentParser xmlDocumentParser = new XmlDocumentParser();

    /** Maps Activity and Positions statement XML into one combined tabular document. */
    public TabularDocument extractFlexStatementDocument(
            List<String> activityStatementXmls,
            List<String> positionStatementXmls
    ) {
        List<String> activityStatements = List.copyOf(
                Objects.requireNonNull(activityStatementXmls, "activityStatementXmls"));
        List<String> positionStatements = List.copyOf(
                Objects.requireNonNull(positionStatementXmls, "positionStatementXmls"));
        List<Element> tradeRows = new ArrayList<>();
        List<Element> openPositionRows = new ArrayList<>();

        for (String statement : activityStatements) {
            Document document = xmlDocumentParser.parse(new ByteArrayInputStream(
                    Objects.requireNonNull(statement, "flexXml").getBytes(StandardCharsets.UTF_8)));
            tradeRows.addAll(findTradeRows(document));
        }
        for (String statement : positionStatements) {
            Document document = xmlDocumentParser.parse(new ByteArrayInputStream(
                    Objects.requireNonNull(statement, "flexXml").getBytes(StandardCharsets.UTF_8)));
            openPositionRows.addAll(findOpenPositionRows(document));
        }

        return createFlexStatementDocument(tradeRows, openPositionRows);
    }

    private TabularDocument createFlexStatementDocument(
            List<Element> tradeRows,
            List<Element> openPositionRows
    ) {
        TabularTable tradesTable = extractTradesTable(tradeRows);
        TabularTable securitiesTable = extractSecuritiesTable(tradeRows);
        TabularTable openPositionsTable = extractOpenPositionsTable(openPositionRows);

        return new TabularDocument(
                "IBKR Flex Query", List.of(tradesTable, securitiesTable, openPositionsTable));
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

    private TabularTable extractOpenPositionsTable(List<Element> openPositionRows) {
        return new TabularTableBuilder<>(
                IbkrNames.Tables.OPEN_POSITIONS,
                OPEN_POSITION_COLUMNS,
                openPositionRows,
                this::createOpenPositionCell)
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

    private List<Element> findOpenPositionRows(Document document) {
        NodeList nodes = document.getElementsByTagName("OpenPosition");
        List<Element> openPositionRows = new ArrayList<>();

        for (int index = 0; index < nodes.getLength(); index++) {
            openPositionRows.add((Element) nodes.item(index));
        }

        return openPositionRows;
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

    private TabularCell createOpenPositionCell(Element positionRow, String columnName, int rowIndex) {
        String rawValue = positionRow.getAttribute(columnName);
        return new TabularCell(
                rawValue,
                normalizeOpenPositionValue(rawValue, columnName),
                new IbkrFlexSourceLocation(IbkrNames.Tables.OPEN_POSITIONS, rowIndex, columnName));
    }

    private static String normalizeOpenPositionValue(String rawValue, String columnName) {
        if (!IbkrNames.OpenPositionColumns.REPORT_DATE.equals(columnName) || rawValue.isBlank()) {
            return rawValue.trim();
        }
        try {
            return LocalDate.parse(rawValue.trim(), FLEX_DATE_FORMATTER).toString();
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid IBKR Open Position reportDate: " + rawValue, exception);
        }
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
