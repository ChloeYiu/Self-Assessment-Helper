package com.helper.util.pdf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class PdfTextExtractorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void extract_returnsTrimmedLinesWithSourceNameAndLocation() throws IOException {
        Path pdfPath = temporaryFolder.newFile("sample.pdf").toPath();
        writePdf(pdfPath);

        PdfTextDocument document = PdfTextExtractor.extract(pdfPath);

        assertEquals("sample.pdf", document.getSourceName().orElseThrow());
        assertEquals(List.of("First page line", "Second page line"), document.getLines()
                .stream()
                .map(PdfTextLine::getText)
                .toList());
        assertEquals(1, document.getLines().get(0).getLocation().orElseThrow().getPageNumber());
        assertEquals(0, document.getLines().get(0).getLocation().orElseThrow().getLineNumber());
        assertEquals(2, document.getLines().get(1).getLocation().orElseThrow().getPageNumber());
        assertEquals(1, document.getLines().get(1).getLocation().orElseThrow().getLineNumber());
    }

    private static void writePdf(Path path) throws IOException {
        try (PDDocument document = new PDDocument()) {
            addPage(document, "First page line");
            addPage(document, "Second page line");
            document.save(path.toFile());
        }
    }

    private static void addPage(PDDocument document, String text) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        try (PDPageContentStream content = new PDPageContentStream(document, page)) {
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
            content.newLineAtOffset(72, 720);
            content.showText(text);
            content.endText();
        }
    }
}
