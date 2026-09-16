package com.helper.ingestion.trade.ibkr;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.Security;
import com.helper.util.table.TabularDocument;
import com.helper.util.table.TabularRow;
import com.helper.util.table.TabularTable;
import java.util.List;
import java.util.Objects;

/**
 * Extracts domain securities from an IBKR Flex securities table.
 */
public class IbkrSecurityExtractor {
    private static final String SECURITIES_TABLE_NAME = "Securities";

    /** Extracts domain securities from a document containing an IBKR Securities table. */
    public List<Security> extractSecurities(TabularDocument document) {
        TabularTable securitiesTable = Objects.requireNonNull(document, "document")
                .getTable(SECURITIES_TABLE_NAME)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + SECURITIES_TABLE_NAME + " table"));

        return securitiesTable.getRows()
                .stream()
                .map(this::extractSecurity)
                .toList();
    }

    private Security extractSecurity(TabularRow row) {
        return new Security(
                row.getText("identifier"),
                row.getText("symbol"),
                row.getText("description"),
                CurrencyCode.parse(row.getText("currency"), "security")
        );
    }
}
