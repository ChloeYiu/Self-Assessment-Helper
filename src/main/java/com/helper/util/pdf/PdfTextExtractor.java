package com.helper.util.pdf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.nio.file.Path;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Extracts raw text lines from a local PDF document.
 */
public final class PdfTextExtractor {
    private PdfTextExtractor() {
    }

    /**
     * Extracts an ordered line view from a local PDF.
     *
     * @param pdfPath local PDF path
     * @return raw PDF text document
     * @throws IOException if the PDF cannot be read
     */
    public static PdfTextDocument extract(Path pdfPath) throws IOException {
        Path path = Objects.requireNonNull(pdfPath, "pdfPath");

        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            return extract(document, path.toFile().getName());
        }
    }

    /**
     * Extracts an ordered line view from PDF bytes.
     *
     * @param pdfBytes PDF bytes
     * @param sourceName human-readable source name
     * @return raw PDF text document
     * @throws IOException if the PDF cannot be read
     */
    public static PdfTextDocument extract(byte[] pdfBytes, String sourceName) throws IOException {
        try (PDDocument document = Loader.loadPDF(Objects.requireNonNull(pdfBytes, "pdfBytes"))) {
            return extract(document, Objects.requireNonNull(sourceName, "sourceName"));
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
