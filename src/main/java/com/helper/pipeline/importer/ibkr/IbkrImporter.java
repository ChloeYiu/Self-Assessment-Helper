package com.helper.pipeline.importer.ibkr;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.ingestion.trade.ibkr.IbkrApiProvider;
import com.helper.ingestion.trade.ibkr.IbkrSecurityExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTradeExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTradeMapper;
import com.helper.pipeline.importer.SourceImportResult;
import com.helper.pipeline.importer.SourceImporter;
import com.helper.util.table.TabularDocument;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Imports normalized IBKR securities and trades for an explicit requested range.
 */
public class IbkrImporter implements SourceImporter<IbkrImportInput> {
    private final IbkrApiProvider apiProvider;
    private final IbkrTradeMapper tradeMapper;
    private final IbkrSecurityExtractor securityExtractor;
    private final IbkrTradeExtractor tradeExtractor;

    /** Creates an importer that builds its provider from the supplied import input. */
    public IbkrImporter() {
        this(null, new IbkrTradeMapper(), new IbkrSecurityExtractor(), new IbkrTradeExtractor());
    }

    IbkrImporter(
            IbkrApiProvider apiProvider,
            IbkrTradeMapper tradeMapper,
            IbkrSecurityExtractor securityExtractor,
            IbkrTradeExtractor tradeExtractor
    ) {
        this.apiProvider = apiProvider;
        this.tradeMapper = Objects.requireNonNull(tradeMapper, "tradeMapper");
        this.securityExtractor = Objects.requireNonNull(securityExtractor, "securityExtractor");
        this.tradeExtractor = Objects.requireNonNull(tradeExtractor, "tradeExtractor");
    }

    /** Retrieves, maps, and normalizes IBKR Flex statements into securities and trades. */
    @Override
    public SourceImportResult importSource(IbkrImportInput input) throws IOException, InterruptedException {
        IbkrImportInput importInput = Objects.requireNonNull(input, "input");
        IbkrApiProvider provider = apiProvider == null
                ? createApiProvider(importInput)
                : apiProvider;
        LocalDate effectiveEndDate = provider.truncateRequestedEndDate(
                importInput.getRequestedStartDate(), importInput.getRequestedEndDate());
        IbkrApiProvider.IbkrApiResult apiResult = provider.getFlexStatementsXml(
                importInput.getRequestedStartDate(), effectiveEndDate);
        TabularDocument document = tradeMapper.extractFlexStatementDocument(apiResult.statementXmls());
        List<Security> securities = securityExtractor.extractSecurities(document);
        List<Trade> trades = new ArrayList<>();

        for (Security security : securities) {
            trades.addAll(tradeExtractor.extractTrades(document, security));
        }

        return new SourceImportResult(
                securities,
                trades,
                importInput.getRequestedStartDate(),
                effectiveEndDate,
                apiResult.issues());
    }

    private static IbkrApiProvider createApiProvider(IbkrImportInput input) {
        if (input.getConfigPath() == null) {
            return new IbkrApiProvider();
        }
        return new IbkrApiProvider(input.getConfigPath());
    }
}
