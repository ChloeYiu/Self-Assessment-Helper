package com.helper.ingestion.util.pdf;

import java.util.Objects;
import java.util.Optional;

/**
 * One line of text inferred from a PDF text extraction pass.
 */
public class PdfTextLine {
    private final String text;
    private final PdfTextLocation location;

    /**
     * Creates an extracted PDF text line without source position.
     *
     * @param text extracted line text
     */
    public PdfTextLine(String text) {
        this(text, null);
    }

    /**
     * Creates an extracted PDF text line.
     *
     * @param text extracted line text
     * @param location optional source position
     */
    public PdfTextLine(String text, PdfTextLocation location) {
        this.text = Objects.requireNonNull(text, "text");
        this.location = location;
    }

    /** Returns the extracted line text. */
    public String getText() {
        return text;
    }

    /** Returns the source position when available. */
    public Optional<PdfTextLocation> getLocation() {
        return Optional.ofNullable(location);
    }
}
