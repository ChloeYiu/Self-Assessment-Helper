package com.helper.ingestion.util.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Model for one logical table row.
 */
public class TabularRow {
    private final int index;
    private final List<TabularCell> cells;
    private final Map<String, Integer> columnIndexesByName;

    public TabularRow(int index, List<TabularCell> cells) {
        this(index, cells, List.of());
    }

    public TabularRow(
            int index,
            List<TabularCell> cells,
            List<TabularColumn> columns) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }

        this.index = index;
        this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
        this.columnIndexesByName = Collections.unmodifiableMap(columns.stream()
                .filter(column -> column.getName().isPresent() || column.getNormalizedName().isPresent())
                .flatMap(column -> List.of(
                        column.getName().map(TabularRow::normalizeColumnLookupKey),
                        column.getNormalizedName().map(TabularRow::normalizeColumnLookupKey)).stream()
                        .flatMap(Optional::stream)
                        .map(name -> Map.entry(name, column.getIndex())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing)));
    }

    public int getIndex() {
        return index;
    }

    public List<TabularCell> getCells() {
        return cells;
    }

    public Optional<TabularCell> getCell(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= cells.size()) {
            return Optional.empty();
        }

        return Optional.of(cells.get(columnIndex));
    }

    public Optional<TabularCell> getCell(String columnName) {
        Integer columnIndex = columnIndexesByName.get(normalizeColumnLookupKey(columnName));

        if (columnIndex == null) {
            return Optional.empty();
        }

        return getCell(columnIndex);
    }

    private static String normalizeColumnLookupKey(String value) {
        return Objects.requireNonNull(value, "value").trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
