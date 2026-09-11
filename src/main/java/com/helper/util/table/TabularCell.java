package com.helper.util.table;

import java.util.Objects;
import java.util.Optional;

/**
 * Model for one logical table cell.
 */
public class TabularCell {
    private final String rawText;
    private final String normalizedText;
    private final TabularSourceLocation sourceLocation;

    public TabularCell(String rawText) {
        this(rawText, normalize(rawText), null);
    }

    public TabularCell(String rawText, TabularSourceLocation sourceLocation) {
        this(rawText, normalize(rawText), sourceLocation);
    }

    public TabularCell(String rawText, String normalizedText, TabularSourceLocation sourceLocation) {
        this.rawText = Objects.requireNonNull(rawText, "rawText");
        this.normalizedText = normalizedText;
        this.sourceLocation = sourceLocation;
    }

    public String getRawText() {
        return rawText;
    }

    public Optional<String> getNormalizedText() {
        return Optional.ofNullable(normalizedText);
    }

    public Optional<TabularSourceLocation> getSourceLocation() {
        return Optional.ofNullable(sourceLocation);
    }

    private static String normalize(String value) {
        return Objects.requireNonNull(value, "value").trim().replaceAll("\\s+", " ");
    }
}
