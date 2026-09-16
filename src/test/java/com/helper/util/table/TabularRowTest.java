package com.helper.util.table;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class TabularRowTest {
    @Test
    public void typedGetters_readNamedCells() {
        TabularRow row = createRow(Map.of(
                "text", "  hello   world  ",
                "amount", " 123.45 ",
                "date", "2026-04-05",
                "type", "BUY"
        ));

        assertEquals("hello world", row.getText("text"));
        assertEquals(0, new BigDecimal("123.45").compareTo(row.getBigDecimal("amount")));
        assertEquals(LocalDate.of(2026, 4, 5), row.getLocalDate("date"));
        assertEquals(TestTradeType.BUY, row.getEnum("type", TestTradeType.class));
    }

    @Test
    public void typedGetters_throwWhenCellIsMissingOrInvalid() {
        TabularRow row = createRow(Map.of(
                "text", " ",
                "amount", "not numeric",
                "date", "not date",
                "type", "UNKNOWN"
        ));

        assertThrows(IllegalArgumentException.class, () -> row.getText("text"));
        assertThrows(IllegalArgumentException.class, () -> row.getBigDecimal("amount"));
        assertThrows(IllegalArgumentException.class, () -> row.getLocalDate("date"));
        assertThrows(IllegalArgumentException.class, () -> row.getEnum("type", TestTradeType.class));
        assertThrows(IllegalArgumentException.class, () -> row.getText("missing"));
    }

    private static TabularRow createRow(Map<String, String> values) {
        return new TabularTableBuilder<>(
                "Test",
                List.of("text", "amount", "date", "type"),
                List.of(values),
                (row, columnName, rowIndex) -> new TabularCell(row.getOrDefault(columnName, ""))
        ).buildTable().getRow(0).orElseThrow();
    }

    private enum TestTradeType {
        BUY,
        SELL
    }
}
