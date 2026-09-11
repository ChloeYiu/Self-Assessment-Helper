package com.helper.util.table;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builds logical tables from source-specific row objects.
 */
public class TabularTableBuilder<TRow> {
    private final String tableName;
    private final List<String> columnNames;
    private final List<TRow> rows;
    private final CreateCellFunction<TRow> createCellFunction;

    public TabularTableBuilder(
            String tableName,
            List<String> columnNames,
            List<TRow> rows,
            CreateCellFunction<TRow> createCellFunction) {
        this.tableName = tableName;
        this.columnNames = List.copyOf(Objects.requireNonNull(columnNames, "columnNames"));
        this.rows = List.copyOf(Objects.requireNonNull(rows, "rows"));
        this.createCellFunction = Objects.requireNonNull(createCellFunction, "createCellFunction");
    }

    public TabularTable buildTable() {
        List<TabularColumn> columns = createColumnDefinitions();
        TabularRow headerRow = createHeaderRow();
        List<TabularRow> rows = createRows(columns);

        return new TabularTable(tableName, columns, headerRow, rows);
    }

    private List<TabularColumn> createColumnDefinitions() {
        List<TabularColumn> columns = new ArrayList<>();

        for (int index = 0; index < columnNames.size(); index++) {
            columns.add(new TabularColumn(index, columnNames.get(index)));
        }

        return columns;
    }

    private TabularRow createHeaderRow() {
        List<TabularCell> cells = columnNames.stream()
                .map(TabularCell::new)
                .toList();

        return new TabularRow(0, cells);
    }

    private List<TabularRow> createRows(List<TabularColumn> columns) {
        List<TabularRow> tableRows = new ArrayList<>();

        for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
            TRow row = rows.get(rowIndex);
            List<TabularCell> cells = new ArrayList<>();

            for (String columnName : columnNames) {
                cells.add(createCellFunction.createCell(row, columnName, rowIndex));
            }

            tableRows.add(new TabularRow(rowIndex, cells, columns));
        }

        return tableRows;
    }

    public interface CreateCellFunction<TRow> {
        TabularCell createCell(TRow row, String columnName, int rowIndex);
    }
}
