package com.helper.util.table;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TabularCellTest {

    @Test
    public void getNormalizedText_trimsAndCollapsesWhitespace() {
        TabularCell cell = new TabularCell("  Currency   Units   per £1  ");

        assertEquals("  Currency   Units   per £1  ", cell.getRawText());
        assertEquals("Currency Units per £1", cell.getNormalizedText().orElseThrow());
    }

    @Test
    public void getBigDecimal_returnsNumberWhenNormalizedTextIsNumeric() {
        TabularCell cell = new TabularCell("  1.2345  ");

        assertEquals(0, new BigDecimal("1.2345").compareTo(cell.getBigDecimal().orElseThrow()));
    }

    @Test
    public void getBigDecimal_returnsEmptyWhenNormalizedTextIsBlankOrNotNumeric() {
        assertFalse(new TabularCell("   ").getBigDecimal().isPresent());
        assertFalse(new TabularCell("not a number").getBigDecimal().isPresent());
    }
}
