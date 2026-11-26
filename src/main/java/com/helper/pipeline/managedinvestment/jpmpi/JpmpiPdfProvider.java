package com.helper.pipeline.managedinvestment.jpmpi;

import com.helper.util.pdf.Pdf;
import com.helper.util.pdf.PdfTextDocument;
import com.helper.util.pdf.PdfTextExtractor;
import java.io.IOException;
import java.util.Objects;

/**
 * Provides ordered text from one selected JPMPI tax-pack PDF.
 *
 * <p>This class owns PDF-to-text extraction only. It does not identify tax-pack sections or
 * interpret reported tax amounts.</p>
 */
public final class JpmpiPdfProvider {
    /**
     * Reads selected PDF bytes into ordered text with source locations.
     *
     * @param pdf selected PDF content
     * @return ordered extracted PDF text
     * @throws IOException if the PDF cannot be read
     */
    public PdfTextDocument loadTextDocument(Pdf pdf) throws IOException {
        return PdfTextExtractor.extract(Objects.requireNonNull(pdf, "pdf"));
    }
}
