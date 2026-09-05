package com.helper.ingestion.trade.ibkr;

import com.helper.ingestion.Extractor;
import com.helper.ingestion.util.table.TabularCell;
import com.helper.ingestion.util.table.TabularDocument;
import com.helper.ingestion.util.table.TabularTable;
import com.helper.ingestion.util.table.TabularTableBuilder;
import com.helper.ingestion.util.api.XmlDocumentParser;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 * Extractor for IBKR Flex trade history payloads.
 */
public class IbkrApiExtractor implements Extractor<TabularDocument> {
    private static final String TRADES_TABLE_NAME = "Trades";
    private static final List<String> PREFERRED_TRADE_COLUMNS = List.of(
            "accountId",
            "assetCategory",
            "symbol",
            "description",
            "conid",
            "tradeDate",
            "settleDateTarget",
            "buySell",
            "quantity",
            "tradePrice",
            "proceeds",
            "ibCommission",
            "currency",
            "fifoPnlRealized");

    private final XmlDocumentParser xmlDocumentParser = new XmlDocumentParser();

    public TabularDocument extractTradeTable(String flexXml) {
        Objects.requireNonNull(flexXml, "flexXml");
        return extractTradeTable(new ByteArrayInputStream(flexXml.getBytes(StandardCharsets.UTF_8)));
    }

    public TabularDocument extractTradeTable(InputStream inputStream) {
        Objects.requireNonNull(inputStream, "inputStream");

        Document document = xmlDocumentParser.parse(inputStream);
        List<Element> tradeRows = findTradeRows(document);
        List<String> columnNames = findTradeColumnNames(tradeRows);
        TabularTable tradesTable = new TabularTableBuilder<>(
                TRADES_TABLE_NAME,
                columnNames,
                tradeRows,
                this::createTradeCell)
                .buildTable();

        return new TabularDocument("IBKR Flex Query", List.of(tradesTable));
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
}
