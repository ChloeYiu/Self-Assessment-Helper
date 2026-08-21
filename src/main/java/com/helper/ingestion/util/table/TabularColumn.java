package com.helper.ingestion.util.table;

import java.util.Objects;
import java.util.Optional;

/**
 * Model for one logical table column.
 */
public class TabularColumn {
    private final int index;
    private final String name;
    private final String normalizedName;

    public TabularColumn(int index) {
        this(index, null, null);
    }

    public TabularColumn(int index, String name) {
        this(index, name, normalizeName(name));
    }

    public TabularColumn(int index, String name, String normalizedName) {
        if (index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }

        this.index = index;
        this.name = name;
        this.normalizedName = normalizedName;
    }

    public int getIndex() {
        return index;
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getNormalizedName() {
        return Optional.ofNullable(normalizedName);
    }

    private static String normalizeName(String value) {
        if (value == null) {
            return null;
        }

        return Objects.requireNonNull(value, "value").trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
