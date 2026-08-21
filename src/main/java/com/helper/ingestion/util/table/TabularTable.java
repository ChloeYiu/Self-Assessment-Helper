package com.helper.ingestion.util.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
}
