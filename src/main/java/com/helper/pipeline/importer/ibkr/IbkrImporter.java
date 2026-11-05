package com.helper.pipeline.importer.ibkr;

import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.ingestion.trade.ibkr.IbkrApiProvider;
import com.helper.ingestion.trade.ibkr.IbkrFlexStatementMapper;
import com.helper.ingestion.trade.ibkr.IbkrSecurityExtractor;
import com.helper.ingestion.trade.ibkr.IbkrPositionExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTradeExtractor;
import com.helper.issue.Issue;
import com.helper.pipeline.importer.SourceImportResult;
import com.helper.pipeline.importer.Position;
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
    private final IbkrFlexStatementMapper flexStatementMapper;
    private final IbkrSecurityExtractor securityExtractor;
    private final IbkrTradeExtractor tradeExtractor;
    private final IbkrPositionExtractor positionExtractor;

    /** Creates an importer that builds its provider from the supplied import input. */
    public IbkrImporter() {
        this(null, new IbkrFlexStatementMapper(), new IbkrSecurityExtractor(), new IbkrTradeExtractor(),
                new IbkrPositionExtractor());
    }

    IbkrImporter(
            IbkrApiProvider apiProvider,
            IbkrFlexStatementMapper flexStatementMapper,
            IbkrSecurityExtractor securityExtractor,
            IbkrTradeExtractor tradeExtractor,
            IbkrPositionExtractor positionExtractor
    ) {
        this.apiProvider = apiProvider;
        this.flexStatementMapper = Objects.requireNonNull(flexStatementMapper, "flexStatementMapper");
        this.securityExtractor = Objects.requireNonNull(securityExtractor, "securityExtractor");
        this.tradeExtractor = Objects.requireNonNull(tradeExtractor, "tradeExtractor");
        this.positionExtractor = Objects.requireNonNull(positionExtractor, "positionExtractor");
    }

    /** Retrieves, maps, and normalizes IBKR Flex statements into securities and trades. */
    @Override
    public SourceImportResult importSource(IbkrImportInput input) throws IOException, InterruptedException {
        IbkrImportInput importInput = Objects.requireNonNull(input, "input");
        IbkrApiProvider provider = apiProvider == null
                ? createApiProvider(importInput)
                : apiProvider;
        IbkrApiProvider.IbkrApiResult activityResult = provider.getActivityStatementsXml(
                importInput.getRequestedStartDate(), importInput.getRequestedEndDate());
        List<Issue> issues = new ArrayList<>(activityResult.issues());
        IbkrApiProvider.IbkrApiResult positionsResult = provider.getPositionStatementXml(
                importInput.getRequestedStartDate());
        issues.addAll(positionsResult.issues());
        TabularDocument document = flexStatementMapper.extractFlexStatementDocument(
                activityResult.statementXmls(), positionsResult.statementXmls());
        List<Security> securities = securityExtractor.extractSecurities(document);
        List<Trade> trades = new ArrayList<>();
        for (Security security : securities) {
            trades.addAll(tradeExtractor.extractTrades(document, security));
        }
        List<Position> positions = positionExtractor.extractPositions(
                document, importInput.getRequestedStartDate());

        return new SourceImportResult(
                securities,
                trades,
                positions,
                importInput.getRequestedStartDate(),
                activityResult.effectiveEndDate(),
                issues);
    }

    private static IbkrApiProvider createApiProvider(IbkrImportInput input) {
        if (input.getConfigPath() == null) {
            return new IbkrApiProvider();
        }
        return new IbkrApiProvider(input.getConfigPath());
    }
}
