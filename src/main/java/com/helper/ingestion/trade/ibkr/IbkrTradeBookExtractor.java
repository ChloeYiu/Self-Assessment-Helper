package com.helper.ingestion.trade.ibkr;

import com.helper.config.TaxYear;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.capitalgain.model.TradeBook;
import com.helper.util.table.TabularDocument;
import java.util.Objects;

/**
 * Extracts a capital-gain trade book from an IBKR Flex trades table.
 */
public class IbkrTradeBookExtractor {
    private final IbkrTradeExtractor tradeExtractor = new IbkrTradeExtractor();

    /** Extracts a trade book for one security and tax year. */
    public TradeBook extractTradeBook(TabularDocument document, Security security, TaxYear taxYear) {
        Security targetSecurity = Objects.requireNonNull(security, "security");
        TradeBook tradeBook = new TradeBook(targetSecurity, Objects.requireNonNull(taxYear, "taxYear"));
        tradeExtractor.extractTrades(Objects.requireNonNull(document, "document"), targetSecurity)
                .forEach(tradeBook::add);

        return tradeBook;
    }

}
