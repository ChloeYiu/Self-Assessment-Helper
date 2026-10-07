package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.Security;
import com.helper.util.table.TabularCell;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularTable;
import com.helper.util.table.TabularTableBuilder;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class IbkrSecurityExtractorTest {
    @Test
    public void extractSecurities_mapsSecuritiesTableToDomainSecurities() {
        TabularDocument document = new TabularDocument(List.of(createSecuritiesTable()));

        List<Security> securities = new IbkrSecurityExtractor().extractSecurities(document);

        assertEquals(1, securities.size());
        Security security = securities.get(0);
        assertEquals("IE00B44Z5B48", security.getIdentifier());
        assertEquals("ACWI", security.getTicker());
        assertEquals("SPDR MSCI ACWI UCITS ETF", security.getName());
        assertEquals(CurrencyCode.USD, security.getCurrencyCode());
    }

    private static TabularTable createSecuritiesTable() {
        return new TabularTableBuilder<>(
                "Securities",
                List.of("identifier", "symbol", "description", "currency"),
                List.of(Map.of(
                        "identifier", "IE00B44Z5B48",
                        "symbol", "ACWI",
                        "description", "SPDR MSCI ACWI UCITS ETF",
                        "currency", "USD"
                )),
                (row, columnName, rowIndex) -> new TabularCell(row.get(columnName))
        ).buildTable();
    }
}
