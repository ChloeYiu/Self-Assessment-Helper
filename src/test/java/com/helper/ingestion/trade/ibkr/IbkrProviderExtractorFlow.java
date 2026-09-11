package com.helper.ingestion.trade.ibkr;

import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularColumn;
import com.helper.util.table.TabularTable;
import de.vandermeer.asciitable.AsciiTable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manual live flow for checking IBKR Flex API download and table extraction.
 *
 * <p>
 * Uses {@code config/ibkr-flex.properties}. Pass explicit from/to dates as
 * arguments, for example {@code 2025-04-06 2026-05-05}.
 */
public class IbkrProviderExtractorFlow {
    private static final Path DEFAULT_CONFIG_PATH = Path.of("config/ibkr-flex.properties");
    private static final int TABLE_WIDTH = 220;

    public static void main(String[] args) throws Exception {
        DateRange dateRange = parseDateRange(args);

        if (!Files.exists(DEFAULT_CONFIG_PATH)) {
            throw new IllegalStateException("Missing IBKR Flex config at " + DEFAULT_CONFIG_PATH);
        }

        IbkrApiProvider provider = new IbkrApiProvider(DEFAULT_CONFIG_PATH);
        IbkrApiExtractor extractor = new IbkrApiExtractor();

        try (InputStream statementXml = provider.getFlexStatement(dateRange.fromDate(), dateRange.toDate())) {
            TabularDocument document = extractor.extractFlexStatementDocument(statementXml);
            printSummary(document, dateRange);
        }
    }

    private static DateRange parseDateRange(String[] args) {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: IbkrProviderExtractorFlow <from-date> <to-date>");
        }

        return new DateRange(LocalDate.parse(args[0]), LocalDate.parse(args[1]));
    }

    private static void printSummary(TabularDocument document, DateRange dateRange) {
        if (dateRange.isEmpty()) {
            System.out.println("effectiveDateRange=empty");
        } else {
            System.out.println("fromDate=" + dateRange.fromDate());
            System.out.println("toDate=" + dateRange.toDate());
        }
        System.out.println("source=" + document.getSourceName().orElse("unknown"));
        System.out.println("tables=" + document.getTables().size());

        for (TabularTable table : document.getTables()) {
            printTableSummary(table);
        }
    }

    private static void printTableSummary(TabularTable table) {
        System.out.println("table=" + table.getName().orElse("unnamed"));
        System.out.println("columns=" + table.getColumns().stream()
                .map(column -> column.getName().orElse("column-" + column.getIndex()))
                .collect(Collectors.joining(",")));
        System.out.println("rows=" + table.getRows().size());

        System.out.println(renderTable(table));
    }

    private static String renderTable(TabularTable table) {
        AsciiTable asciiTable = new AsciiTable();
        asciiTable.addRule();
        asciiTable.addRow(createHeaderRow(table));
        asciiTable.addRule();

        for (int rowIndex = 0; rowIndex < table.getRows().size(); rowIndex++) {
            asciiTable.addRow(createDataRow(table, rowIndex));
            asciiTable.addRule();
        }

        return asciiTable.render(TABLE_WIDTH);
    }

    private static List<String> createHeaderRow(TabularTable table) {
        List<String> headerRow = new ArrayList<>();
        headerRow.add("row");

        table.getColumns().stream()
                .map(IbkrProviderExtractorFlow::getColumnName)
                .forEach(headerRow::add);

        return headerRow;
    }

    private static List<String> createDataRow(TabularTable table, int rowIndex) {
        List<String> dataRow = new ArrayList<>();
        dataRow.add(String.valueOf(rowIndex));

        for (TabularColumn column : table.getColumns()) {
            dataRow.add(table.getCell(rowIndex, column.getIndex())
                    .map(cell -> cell.getRawText())
                    .orElse(""));
        }

        return dataRow;
    }

    private static String getColumnName(TabularColumn column) {
        return column.getName().orElse("column-" + column.getIndex());
    }

    private record DateRange(LocalDate fromDate, LocalDate toDate) {
        private boolean isEmpty() {
            return toDate.isBefore(fromDate);
        }
    }
}
