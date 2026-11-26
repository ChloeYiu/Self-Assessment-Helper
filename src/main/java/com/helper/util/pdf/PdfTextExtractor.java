package com.helper.util.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Extracts raw text lines from PDF content.
 */
public final class PdfTextExtractor {
    private PdfTextExtractor() {
    }

    /** Extracts an ordered line view from PDF content. */
    public static PdfTextDocument extract(Pdf pdf) throws IOException {
        Pdf source = Objects.requireNonNull(pdf, "pdf");
        try (PDDocument document = Loader.loadPDF(source.getBytes())) {
            return extract(document, source.getFileName());
        }
    }

    private static PdfTextDocument extract(PDDocument document, String sourceName) throws IOException {
        List<PdfTextLine> lines = new ArrayList<>();
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);

        for (int pageNumber = 1; pageNumber <= document.getNumberOfPages(); pageNumber++) {
            stripper.setStartPage(pageNumber);
            stripper.setEndPage(pageNumber);
            addPageLines(lines, stripper.getText(document), pageNumber);
        }

        return new PdfTextDocument(sourceName, lines);
    }

    private static void addPageLines(List<PdfTextLine> lines, String pageText, int pageNumber) {
        pageText.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(line -> lines.add(new PdfTextLine(line, new PdfTextLocation(pageNumber, lines.size()))));
    }
}
