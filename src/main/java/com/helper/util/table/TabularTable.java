package com.helper.util.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Model for one logical table extracted from a source document.
 */
public class TabularTable {
    private final String name;
    private final List<TabularColumn> columns;
    private final TabularRow headerRow;
    private final List<TabularRow> rows;

    public TabularTable(List<TabularRow> rows) {
        this(null, List.of(), null, rows);
    }

    public TabularTable(String name, List<TabularColumn> columns, TabularRow headerRow, List<TabularRow> rows) {
        this.name = name;
        this.columns = Collections.unmodifiableList(new ArrayList<>(columns));
        this.headerRow = headerRow;
        this.rows = Collections.unmodifiableList(new ArrayList<>(rows));
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public List<TabularColumn> getColumns() {
        return columns;
    }

    public Optional<TabularRow> getHeaderRow() {
        return Optional.ofNullable(headerRow);
    }

    public List<TabularRow> getRows() {
        return rows;
    }

    public Optional<TabularRow> getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            return Optional.empty();
        }

        return Optional.of(rows.get(rowIndex));
    }

    public Optional<TabularColumn> getColumn(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= columns.size()) {
            return Optional.empty();
        }

        return Optional.of(columns.get(columnIndex));
    }

    public Optional<TabularColumn> getColumn(String columnName) {
        String normalizedColumnName = normalizeColumnLookupKey(columnName);

        return columns.stream()
                .filter(column -> column.getName()
                        .map(TabularTable::normalizeColumnLookupKey)
                        .filter(normalizedColumnName::equals)
                        .isPresent()
                        || column.getNormalizedName()
                                .map(TabularTable::normalizeColumnLookupKey)
                                .filter(normalizedColumnName::equals)
                                .isPresent())
                .findFirst();
    }

    public Optional<TabularCell> getCell(int rowIndex, int columnIndex) {
        return getRow(rowIndex).flatMap(row -> row.getCell(columnIndex));
    }

    public Optional<TabularCell> getCell(int rowIndex, String columnName) {
        return getRow(rowIndex).flatMap(row -> row.getCell(columnName));
    }

    private static String normalizeColumnLookupKey(String value) {
        return Objects.requireNonNull(value, "value").trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
