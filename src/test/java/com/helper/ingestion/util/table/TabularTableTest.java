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
        TabularRow dataRow = table.getRow(0).orElseThrow();

        assertEquals(sourceName, document.getSourceName().orElseThrow());
        assertEquals("Test Table", document.getTables().get(0).getName().orElseThrow());
        assertEquals("Test Table", table.getName().orElseThrow());
        assertEquals(3, table.getColumns().size());
        assertEquals(2, table.getRows().size());
        assertEquals(0, dataRow.getIndex());
        assertEquals(1, table.getRow(1).orElseThrow().getIndex());
        assertEquals("description", table.getColumn(1).orElseThrow().getName().orElseThrow());
        assertEquals(2, table.getColumn("TEST AMOUNT").orElseThrow().getIndex());
        assertEquals("test date", table.getColumn(0).orElseThrow().getNormalizedName().orElseThrow());
        assertEquals("test date", table.getHeaderRow().orElseThrow().getCell(0).orElseThrow().getRawText());

        assertEquals(" 2026-01-01 ", table.getCell(0, 0).orElseThrow().getRawText());
        assertEquals("100.00", table.getCell(0, "test amount").orElseThrow().getRawText());
        assertEquals("2026-01-01", table.getCell(0, "test date").orElseThrow().getNormalizedText().orElseThrow());
        assertEquals(
                "First test row",
                table.getCell(0, "DESCRIPTION").orElseThrow().getNormalizedText().orElseThrow());
        assertEquals("100.00", table.getCell(0, "test amount").orElseThrow().getRawText());
        TestSourceLocation actualAmountLocation =
                (TestSourceLocation) table.getCell(0, 2).orElseThrow().getSourceLocation().orElseThrow();
        assertEquals("built-source", actualAmountLocation.getSourceName());
        assertEquals(0, actualAmountLocation.getRowNumber());
        assertEquals(2, actualAmountLocation.getColumnIndex());
        assertEquals("2026-01-02", table.getCell(1, "test date").orElseThrow().getRawText());
        assertEquals("200.00", table.getCell(1, "test amount").orElseThrow().getRawText());

        assertFalse(table.getRow(99).isPresent());
        assertFalse(table.getColumn(99).isPresent());
        assertFalse(table.getColumn("missing amount").isPresent());
        assertFalse(table.getCell(99, 0).isPresent());
        assertFalse(table.getCell(0, 99).isPresent());
        assertFalse(table.getCell(0, "missing amount").isPresent());
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
