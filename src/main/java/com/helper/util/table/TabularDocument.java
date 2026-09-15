package com.helper.util.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Model for table-like content extracted from one source document.
 */
public class TabularDocument {
    private final String sourceName;
    private final List<TabularTable> tables;
    private final Map<String, TabularTable> tablesByName;

    public TabularDocument(List<TabularTable> tables) {
        this(null, tables);
    }

    public TabularDocument(String sourceName, List<TabularTable> tables) {
        this.sourceName = sourceName;
        this.tables = Collections.unmodifiableList(new ArrayList<>(tables));
        this.tablesByName = Collections.unmodifiableMap(createTablesByName(this.tables));
    }

    public Optional<String> getSourceName() {
        return Optional.ofNullable(sourceName);
    }

    public List<TabularTable> getTables() {
        return tables;
    }

    public Optional<TabularTable> getTable(String tableName) {
        return Optional.ofNullable(tablesByName.get(tableName));
    }

    private Map<String, TabularTable> createTablesByName(List<TabularTable> tables) {
        Map<String, TabularTable> map = new HashMap<>();
        for (TabularTable table : tables) {
            table.getName().ifPresent(name -> map.put(name, table));
        }
        return map;
    }
}
