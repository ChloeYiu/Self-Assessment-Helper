package com.helper.pipeline.managedinvestment.jpmpi;

import com.helper.util.pdf.Pdf;
import com.helper.util.pdf.PdfTextDocument;
import java.io.IOException;
import java.util.Objects;

/**
 * Imports one J.P. Morgan Personal Investing annual source PDF into reported tax facts.
 */
public final class JpmpiImporter {
    private final JpmpiPdfProvider pdfProvider;
    private final JpmpiMapper mapper;

    /** Creates an importer with the JPMPI PDF provider and deterministic source mapper. */
    public JpmpiImporter() {
        this(new JpmpiPdfProvider(), new JpmpiMapper());
    }

    JpmpiImporter(JpmpiPdfProvider pdfProvider, JpmpiMapper mapper) {
        this.pdfProvider = Objects.requireNonNull(pdfProvider, "pdfProvider");
        this.mapper = Objects.requireNonNull(mapper, "mapper");
    }

    /**
     * Imports reported facts from one selected JPMPI annual tax pack.
     *
     * @param pdf selected PDF content
     * @return provider-reported tax facts for that pack
     * @throws IOException if the selected PDF cannot be read
     */
    public JpmpiImportResult importSource(Pdf pdf) throws IOException {
        PdfTextDocument textDocument = pdfProvider.loadTextDocument(Objects.requireNonNull(pdf, "pdf"));
        return mapper.map(textDocument);
    }
}
