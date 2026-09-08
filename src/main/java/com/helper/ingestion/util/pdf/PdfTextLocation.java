package com.helper.ingestion.util.pdf;

/**
 * Source position for text extracted from a PDF.
 */
public class PdfTextLocation {
    private final int pageNumber;
    private final int lineNumber;

    /**
     * Creates a PDF text location.
     *
     * @param pageNumber one-based PDF page number
     * @param lineNumber zero-based extracted line number within the document
     */
    public PdfTextLocation(int pageNumber, int lineNumber) {
        if (pageNumber < 1) {
            throw new IllegalArgumentException("pageNumber must be positive");
        }
        if (lineNumber < 0) {
            throw new IllegalArgumentException("lineNumber must not be negative");
        }

        this.pageNumber = pageNumber;
        this.lineNumber = lineNumber;
    }

    /** Returns the one-based PDF page number. */
    public int getPageNumber() {
        return pageNumber;
    }

    /** Returns the zero-based extracted line number within the document. */
    public int getLineNumber() {
        return lineNumber;
    }
}
