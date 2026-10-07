package com.helper.pipeline.ibkr;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.ingestion.trade.ibkr.IbkrApiProvider;
import com.helper.ingestion.trade.ibkr.IbkrSecurityExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTradeExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTradeMapper;
import com.helper.pipeline.SourceImportResult;
import com.helper.pipeline.SourceImporter;
import com.helper.util.table.TabularDocument;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Imports normalized IBKR securities and trades for an explicit requested range.
 */
public class IbkrImporter implements SourceImporter<IbkrImportInput> {
    private final IbkrTradeMapper tradeMapper = new IbkrTradeMapper();
    private final IbkrSecurityExtractor securityExtractor = new IbkrSecurityExtractor();
    private final IbkrTradeExtractor tradeExtractor = new IbkrTradeExtractor();

    /**
     * Retrieves one IBKR Flex statement and converts its detected securities and trades.
     */
    @Override
    public SourceImportResult importSource(IbkrImportInput input) throws IOException, InterruptedException {
        IbkrImportInput importInput = Objects.requireNonNull(input, "input");
        IbkrApiProvider apiProvider = createApiProvider(importInput);
        String flexStatementXml = apiProvider.getFlexStatementXml(
                importInput.getRequestedStartDate(),
                importInput.getRequestedEndDate());
        TabularDocument document = tradeMapper.extractFlexStatementDocument(flexStatementXml);
        List<Security> securities = securityExtractor.extractSecurities(document);
        List<Trade> trades = new ArrayList<>();
        for (Security security : securities) {
            trades.addAll(tradeExtractor.extractTrades(document, security));
        }
        return new SourceImportResult(
                securities,
                trades,
                importInput.getRequestedStartDate(),
                importInput.getRequestedEndDate());
    }

    private IbkrApiProvider createApiProvider(IbkrImportInput input) {
        if (input.getConfigPath() == null) {
            return new IbkrApiProvider();
        }
        return new IbkrApiProvider(input.getConfigPath());
    }
}
