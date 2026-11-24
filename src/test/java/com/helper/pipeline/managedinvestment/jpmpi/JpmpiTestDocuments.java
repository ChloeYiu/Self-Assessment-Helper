package com.helper.pipeline.managedinvestment.jpmpi;

import com.helper.util.pdf.PdfTextDocument;
import com.helper.util.pdf.PdfTextLine;
import com.helper.util.pdf.PdfTextLocation;
import com.helper.util.pdf.Pdf;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Controlled JPMPI source documents shared by managed-investment tests. */
final class JpmpiTestDocuments {
    private static final List<String> SUMMARY_LINES = List.of(
            "Account NEWJRFP2",
            "2025/26 tax year",
            "Foreign interest USD 1.20",
            "Foreign interest EUR 2.30",
            "Foreign dividend USD 3.40",
            "UK bank interest untaxed 4.50",
            "capital loss of £6.70");

    private JpmpiTestDocuments() {
    }

    static PdfTextDocument summaryDocument() {
        return new PdfTextDocument("jpmpi-2025-26.pdf", List.of(
                new PdfTextLine("Cover", new PdfTextLocation(1, 0)),
                line(0, "Account NEWJRFP2"),
                line(1, "2025/26 tax year"),
                line(2, "Foreign interest USD 1.20"),
                line(3, "Foreign interest EUR 2.30"),
                line(4, "Foreign dividend USD 3.40"),
                line(5, "UK bank interest untaxed 4.50"),
                line(6, "capital loss of £6.70"),
                new PdfTextLine("Foreign dividend USD 99.99", new PdfTextLocation(3, 8))));
    }

    static PdfTextDocument noTotalsDocument() {
        return new PdfTextDocument("empty-jpmpi-2025-26.pdf", List.of(
                line(0, "Account NEWJRFP2"),
                line(1, "2025/26 tax year")));
    }

    static Pdf summaryPdf() throws IOException {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            addPage(document, List.of("Cover"));
            addPage(document, SUMMARY_LINES);
            document.save(output);
            return new Pdf(output.toByteArray(), "jpmpi-2025-26.pdf");
        }
    }

    private static PdfTextLine line(int lineNumber, String text) {
        return new PdfTextLine(text, new PdfTextLocation(2, lineNumber + 1));
    }

    private static void addPage(PDDocument document, List<String> lines) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            content.newLineAtOffset(72, 720);
            for (String line : lines) {
                content.showText(line);
                content.newLineAtOffset(0, -18);
            }
            content.endText();
        }
    }
}
