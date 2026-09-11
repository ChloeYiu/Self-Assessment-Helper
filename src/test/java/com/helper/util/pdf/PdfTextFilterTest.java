package com.helper.util.pdf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.junit.Test;

public class PdfTextFilterTest {
    private final PdfTextFilter filter = new PdfTextFilter();

    @Test
    public void matchingText_returnsAllTextMatchesInOrder() {
        PdfTextDocument document = document(
                "Summary Section",
                "Detailed Activity",
                "01/02/25 01/03/25 Example debit 10.00 990.00",
                "Example continuation detail",
                "01/04/25 01/05/25 Example credit 20.00 1010.00",
                "Total 30.00 1010.00");

        List<PdfTextLine> transactionStarts = filter.matchingText(
                document,
                text -> text.matches("\\d{2}/\\d{2}/\\d{2} \\d{2}/\\d{2}/\\d{2} .*"));

        assertEquals(2, transactionStarts.size());
        assertEquals("01/02/25 01/03/25 Example debit 10.00 990.00", transactionStarts.get(0).getText());
        assertEquals("01/04/25 01/05/25 Example credit 20.00 1010.00", transactionStarts.get(1).getText());
    }

    @Test
    public void matching_canUseLineLocation() {
        PdfTextLine pageOneLine = new PdfTextLine("page one", new PdfTextLocation(1, 0));
        PdfTextLine pageTwoLine = new PdfTextLine("page two", new PdfTextLocation(2, 1));
        PdfTextDocument document = new PdfTextDocument(List.of(pageOneLine, pageTwoLine));

        List<PdfTextLine> pageTwoLines = filter.matching(
                document,
                line -> line.getLocation().orElseThrow().getPageNumber() == 2);

        assertEquals(List.of(pageTwoLine), pageTwoLines);
    }

    @Test
    public void firstMatchingText_returnsFirstTextMatch() {
        PdfTextDocument document = document("alpha", "Total first", "Total second");

        PdfTextLine firstTotal = filter.firstMatchingText(document, text -> text.startsWith("Total")).orElseThrow();

        assertEquals("Total first", firstTotal.getText());
        assertTrue(filter.firstMatchingText(document, text -> text.equals("missing")).isEmpty());
    }

    @Test
    public void between_returnsBoundedRangeWithIncludeOptions() {
        PdfTextDocument document = document("before", "START", "one", "two", "END", "after");

        assertTexts(
                List.of("one", "two"),
                filter.between(document, text -> text.equals("START"), text -> text.equals("END"), false, false));
        assertTexts(
                List.of("START", "one", "two", "END"),
                filter.between(document, text -> text.equals("START"), text -> text.equals("END"), true, true));
        assertTrue(filter.between(document, text -> text.equals("missing"), text -> text.equals("END"), false, false)
                .isEmpty());
        assertTrue(filter.between(document, text -> text.equals("START"), text -> text.equals("missing"), false, false)
                .isEmpty());
    }

    @Test
    public void around_returnsBoundedWindowAroundExistingLine() {
        PdfTextLine center = new PdfTextLine("center");
        PdfTextDocument document = new PdfTextDocument(List.of(
                new PdfTextLine("zero"),
                new PdfTextLine("one"),
                center,
                new PdfTextLine("three")));

        assertTexts(List.of("one", "center", "three"), filter.around(document, center, 1, 1));
        assertTexts(List.of("zero", "one", "center"), filter.around(document, center, 5, 0));
        assertTrue(filter.around(document, new PdfTextLine("center"), 1, 1).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> filter.around(document, center, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> filter.around(document, center, 0, -1));
    }

    private static PdfTextDocument document(String... lines) {
        return new PdfTextDocument(List.of(lines).stream().map(PdfTextLine::new).toList());
    }

    private static void assertTexts(List<String> expected, List<PdfTextLine> actual) {
        assertEquals(expected, actual.stream().map(PdfTextLine::getText).toList());
    }
}
