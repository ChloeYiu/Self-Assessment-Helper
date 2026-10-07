package com.helper.util.table;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

public class TabularDocumentTest {

    @Test
    public void getTable_returnsNamedTableWithoutLosingOrderedTables() {
        TabularTable first = new TabularTable("first", List.of(), null, List.of());
        TabularTable second = new TabularTable("second", List.of(), null, List.of());
        TabularTable unnamed = new TabularTable(List.of());

        TabularDocument document = new TabularDocument("source", List.of(first, unnamed, second));

        assertEquals("source", document.getSourceName().orElseThrow());
        assertEquals(3, document.getTables().size());
        assertSame(first, document.getTable("first").orElseThrow());
        assertSame(second, document.getTable("second").orElseThrow());
        assertFalse(document.getTable("missing").isPresent());
    }

    @Test
    public void getTables_returnsImmutableList() {
        TabularDocument document = new TabularDocument(List.of(new TabularTable(List.of())));

        assertThrows(UnsupportedOperationException.class, () -> document.getTables().add(new TabularTable(List.of())));
    }
}
