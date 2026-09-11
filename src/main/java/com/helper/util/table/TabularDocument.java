package com.helper.util.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Model for table-like content extracted from one source document.
 */
public class TabularDocument {
    private final String sourceName;
    private final List<TabularTable> tables;

    public TabularDocument(List<TabularTable> tables) {
        this(null, tables);
    }

    public TabularDocument(String sourceName, List<TabularTable> tables) {
        this.sourceName = sourceName;
        this.tables = Collections.unmodifiableList(new ArrayList<>(tables));
    }

    public Optional<String> getSourceName() {
        return Optional.ofNullable(sourceName);
    }

    public List<TabularTable> getTables() {
        return tables;
    }
}
