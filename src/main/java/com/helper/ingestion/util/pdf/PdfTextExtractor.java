package com.helper.ingestion.util.pdf;

import java.io.File;
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
public class PdfTextExtractor {
    /** Creates a PDF text extractor. */
    public PdfTextExtractor() {
    }

    /**
     * Extracts an ordered line view from a local PDF.
     *
     * @param pdfPath local PDF path
     * @return raw PDF text document
     * @throws IOException if the PDF cannot be read
     */
    public PdfTextDocument extract(Path pdfPath) throws IOException {
        Path path = Objects.requireNonNull(pdfPath, "pdfPath");
        List<PdfTextLine> lines = new ArrayList<>();

        try (PDDocument document = Loader.loadPDF(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);

            for (int pageNumber = 1; pageNumber <= document.getNumberOfPages(); pageNumber++) {
                stripper.setStartPage(pageNumber);
                stripper.setEndPage(pageNumber);
                addPageLines(lines, stripper.getText(document), pageNumber);
            }
        }

        File file = path.toFile();
        return new PdfTextDocument(file.getName(), lines);
    }

    private static void addPageLines(List<PdfTextLine> lines, String pageText, int pageNumber) {
        pageText.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .forEach(line -> lines.add(new PdfTextLine(line, new PdfTextLocation(pageNumber, lines.size()))));
    }
}
