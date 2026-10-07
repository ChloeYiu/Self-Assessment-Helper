package com.helper.util.api;

import com.helper.util.table.TabularTable;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class CsvDocumentParserTest {

    @Test
    public void parse_usesFirstRecordAsHeaderWhenColumnNamesAreNotProvided() {
        String csv = "Currency Code,Currency Units per £1\nUSD,1.25\nHKD,9.80\n";

        TabularTable table = new CsvDocumentParser().parse(csv, "rates");

        assertEquals("rates", table.getName().orElseThrow());
        assertEquals(2, table.getColumns().size());
        assertEquals(2, table.getRows().size());
        assertEquals("Currency Code", table.getHeaderRow().orElseThrow().getCell(0).orElseThrow().getRawText());
        assertEquals("USD", table.getCell(0, "Currency Code").orElseThrow().getRawText());
        assertEquals("1.25", table.getCell(0, "Currency Units per £1").orElseThrow().getRawText());
        assertEquals("HKD", table.getCell(1, "Currency Code").orElseThrow().getRawText());
    }

    @Test
    public void parse_usesProvidedColumnNamesWhenCsvHasNoHeader() {
        String csv = "USD,1.25\nHKD,9.80\n";

        TabularTable table = new CsvDocumentParser().parse(
                csv,
                "rates",
                List.of("Currency Code", "Currency Units per £1"));

        assertEquals(2, table.getRows().size());
        assertEquals("Currency Code", table.getHeaderRow().orElseThrow().getCell(0).orElseThrow().getRawText());
        assertEquals("USD", table.getCell(0, "Currency Code").orElseThrow().getRawText());
        assertEquals("1.25", table.getCell(0, "Currency Units per £1").orElseThrow().getRawText());
    }

    @Test
    public void parse_handlesQuotedCommasEscapedQuotesAndMissingTrailingValues() {
        String csv = "Code,Description,Rate\nUSD,\"Dollar, US\",1.25\nHKD,\"Dollar \"\"HK\"\"\"\n";

        TabularTable table = new CsvDocumentParser().parse(csv);

        assertEquals("Dollar, US", table.getCell(0, "Description").orElseThrow().getRawText());
        assertEquals("Dollar \"HK\"", table.getCell(1, "Description").orElseThrow().getRawText());
        assertEquals("", table.getCell(1, "Rate").orElseThrow().getRawText());
    }

    @Test
    public void parse_skipsBlankDataRows() {
        String csv = "Code,Rate\nUSD,1.25\n   ,   \nHKD,9.80\n";

        TabularTable table = new CsvDocumentParser().parse(csv);

        assertEquals(2, table.getRows().size());
        assertEquals("USD", table.getCell(0, "Code").orElseThrow().getRawText());
        assertEquals("HKD", table.getCell(1, "Code").orElseThrow().getRawText());
    }

    @Test
    public void parse_throwsWhenHeaderedCsvIsEmpty() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new CsvDocumentParser().parse(""));

        assertEquals("headered CSV must contain a header row", exception.getMessage());
    }
}
