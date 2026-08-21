package com.helper.ingestion.util.table;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

public class TabularTableTest {

    @Test
    public void tableModel_supportsGeneralDownstreamUsage() {
        String sourceName = "test-source";
        List<TestSourceRow> rows = List.of(
                new TestSourceRow(" 2026-01-01 ", "First   test  row", "100.00"),
                new TestSourceRow("2026-01-02", "Second test row", "200.00"));
        List<String> columnNames = List.of("test date", "description", "test amount");

        TabularTable table = new TabularTableBuilder<>(
                "Test Table",
                columnNames,
                rows,
                (sourceRow, columnName, rowIndex) -> new TabularCell(
                        sourceRow.getColumnValue(columnName),
                        new TestSourceLocation("built-source", rowIndex, columnNames.indexOf(columnName))))
                .buildTable();
        TabularDocument document = new TabularDocument(sourceName, List.of(table));
        TabularRow dataRow = table.getRows().get(0);
        TabularRow secondDataRow = table.getRows().get(1);

        assertEquals(sourceName, document.getSourceName().orElseThrow());
        assertEquals("Test Table", document.getTables().get(0).getName().orElseThrow());
        assertEquals("Test Table", table.getName().orElseThrow());
        assertEquals(3, table.getColumns().size());
        assertEquals(2, table.getRows().size());
        assertEquals("test date", table.getColumns().get(0).getNormalizedName().orElseThrow());
        assertEquals("test date", table.getHeaderRow().orElseThrow().getCell(0).orElseThrow().getRawText());

        assertEquals(" 2026-01-01 ", dataRow.getCell(0).orElseThrow().getRawText());
        assertEquals("2026-01-01", dataRow.getCell("test date").orElseThrow().getNormalizedText().orElseThrow());
        assertEquals(
                "First test row",
                dataRow.getCell("DESCRIPTION").orElseThrow().getNormalizedText().orElseThrow());
        assertEquals("100.00", dataRow.getCell("test amount").orElseThrow().getRawText());
        TestSourceLocation actualAmountLocation =
                (TestSourceLocation) dataRow.getCell(2).orElseThrow().getSourceLocation().orElseThrow();
        assertEquals("built-source", actualAmountLocation.getSourceName());
        assertEquals(0, actualAmountLocation.getRowNumber());
        assertEquals(2, actualAmountLocation.getColumnIndex());
        assertEquals("2026-01-02", secondDataRow.getCell("test date").orElseThrow().getRawText());
        assertEquals("200.00", secondDataRow.getCell("test amount").orElseThrow().getRawText());

        assertFalse(dataRow.getCell(99).isPresent());
        assertFalse(dataRow.getCell("missing amount").isPresent());
        assertThrows(UnsupportedOperationException.class, () -> table.getRows().add(dataRow));
        assertThrows(UnsupportedOperationException.class, () -> dataRow.getCells().add(new TabularCell("extra")));
    }

    private static class TestSourceRow {
        private final String testDate;
        private final String description;
        private final String testAmount;

        private TestSourceRow(String testDate, String description, String testAmount) {
            this.testDate = testDate;
            this.description = description;
            this.testAmount = testAmount;
        }

        private String getColumnValue(String columnName) {
            if ("test date".equals(columnName)) {
                return testDate;
            }

            if ("description".equals(columnName)) {
                return description;
            }

            if ("test amount".equals(columnName)) {
                return testAmount;
            }

            return "";
        }
    }

    private static class TestSourceLocation implements TabularSourceLocation {
        private final String sourceName;
        private final int rowNumber;
        private final int columnIndex;

        private TestSourceLocation(String sourceName, int rowNumber, int columnIndex) {
            this.sourceName = sourceName;
            this.rowNumber = rowNumber;
            this.columnIndex = columnIndex;
        }

        private String getSourceName() {
            return sourceName;
        }

        private int getRowNumber() {
            return rowNumber;
        }

        private int getColumnIndex() {
            return columnIndex;
        }
    }
}
