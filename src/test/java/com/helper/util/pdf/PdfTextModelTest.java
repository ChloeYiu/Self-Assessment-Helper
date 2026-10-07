package com.helper.util.pdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class PdfTextModelTest {
    @Test
    public void location_storesPageAndDocumentLineNumber() {
        PdfTextLocation location = new PdfTextLocation(2, 7);

        assertEquals(2, location.getPageNumber());
        assertEquals(7, location.getLineNumber());
        assertThrows(IllegalArgumentException.class, () -> new PdfTextLocation(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new PdfTextLocation(1, -1));
    }

    @Test
    public void line_supportsTextWithOptionalLocation() {
        PdfTextLocation location = new PdfTextLocation(1, 0);
        PdfTextLine locatedLine = new PdfTextLine("located", location);
        PdfTextLine textOnlyLine = new PdfTextLine("text only");

        assertEquals("located", locatedLine.getText());
        assertEquals(location, locatedLine.getLocation().orElseThrow());
        assertEquals("text only", textOnlyLine.getText());
        assertFalse(textOnlyLine.getLocation().isPresent());
        assertThrows(NullPointerException.class, () -> new PdfTextLine(null));
    }

    @Test
    public void document_storesSourceNameAndDefensiveLineCopy() {
        PdfTextLine firstLine = new PdfTextLine("first");
        List<PdfTextLine> sourceLines = new ArrayList<>(List.of(firstLine));
        PdfTextDocument document = new PdfTextDocument("source.pdf", sourceLines);

        sourceLines.add(new PdfTextLine("later mutation"));

        assertEquals("source.pdf", document.getSourceName().orElseThrow());
        assertEquals(1, document.getLines().size());
        assertEquals(firstLine, document.getLines().get(0));
        assertThrows(UnsupportedOperationException.class, () -> document.getLines().add(new PdfTextLine("extra")));

        PdfTextDocument unnamedDocument = new PdfTextDocument(List.of(firstLine));
        assertTrue(unnamedDocument.getSourceName().isEmpty());
    }
}
