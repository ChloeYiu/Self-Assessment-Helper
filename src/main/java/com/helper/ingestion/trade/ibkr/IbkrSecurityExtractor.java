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
    /** Extracts domain securities from a document containing an IBKR Securities table. */
    public List<Security> extractSecurities(TabularDocument document) {
        TabularTable securitiesTable = Objects.requireNonNull(document, "document")
                .getTable(IbkrNames.Tables.SECURITIES)
                .orElseThrow(() -> new IllegalArgumentException(
                        "document must contain " + IbkrNames.Tables.SECURITIES + " table"));

        return securitiesTable.getRows()
                .stream()
                .map(this::extractSecurity)
                .toList();
    }

    private Security extractSecurity(TabularRow row) {
        return new Security(
                row.getText(IbkrNames.SecurityColumns.IDENTIFIER),
                row.getText(IbkrNames.SecurityColumns.SYMBOL),
                row.getText(IbkrNames.SecurityColumns.DESCRIPTION),
                CurrencyCode.parse(row.getText(IbkrNames.SecurityColumns.CURRENCY), "security")
        );
    }
}
