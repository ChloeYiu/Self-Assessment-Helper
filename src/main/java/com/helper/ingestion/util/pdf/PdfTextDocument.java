package com.helper.ingestion.util.pdf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Raw text extracted from a PDF as an ordered list of inferred lines.
 */
public class PdfTextDocument {
    private final String sourceName;
    private final List<PdfTextLine> lines;

    /**
     * Creates a PDF text document without a source name.
     *
     * @param lines extracted text lines in reading order
     */
    public PdfTextDocument(List<PdfTextLine> lines) {
        this(null, lines);
    }

    /**
     * Creates a PDF text document.
     *
     * @param sourceName optional source document name
     * @param lines extracted text lines in reading order
     */
    public PdfTextDocument(String sourceName, List<PdfTextLine> lines) {
        this.sourceName = sourceName;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }

    /** Returns the optional source document name. */
    public Optional<String> getSourceName() {
        return Optional.ofNullable(sourceName);
    }

    /** Returns extracted text lines in reading order. */
    public List<PdfTextLine> getLines() {
        return lines;
    }
}
