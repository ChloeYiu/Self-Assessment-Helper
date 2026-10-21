package com.helper.pipeline.assembly;

import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigs;
import com.helper.pipeline.TaxPipelineInput;
import com.helper.pipeline.consolidation.PreparedSource;
import com.helper.pipeline.consolidation.SourceConsolidationResult;
import com.helper.pipeline.consolidation.SourceConsolidator;
import com.helper.pipeline.importer.ibkr.IbkrImportInput;
import com.helper.pipeline.importer.ibkr.IbkrImporter;
import com.helper.pipeline.importer.SourceImportResult;
import com.helper.pipeline.support.SecuritySupportResolver;
import com.helper.pipeline.support.SecuritySupportResult;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Coordinates one IBKR source import, support resolution, and consolidation for one tax run. */
public class InvestmentAssemblyPipeline {
    private final Path ibkrConfigPath;
    private final IbkrImporter ibkrImporter;
    private final SecurityConfigs securityConfigs;
    private final Set<DividendCalculationMethodType> executableDividendMethods;
    private final SecuritySupportResolver securitySupportResolver;
    private final SourceConsolidator sourceConsolidator;

    /**
     * Creates the investment-assembly coordinator for one configured IBKR source.
     *
     * @param ibkrConfigPath optional IBKR Flex configuration path
     * @param ibkrImporter IBKR provider importer
     * @param securityConfigs tax treatment configuration by ISIN
     * @param executableDividendMethods dividend methods executable in this application path
     * @param securitySupportResolver security support resolver
     * @param sourceConsolidator source fact consolidator
     */
    public InvestmentAssemblyPipeline(
            Path ibkrConfigPath,
            IbkrImporter ibkrImporter,
            SecurityConfigs securityConfigs,
            Set<DividendCalculationMethodType> executableDividendMethods,
            SecuritySupportResolver securitySupportResolver,
            SourceConsolidator sourceConsolidator
    ) {
        this.ibkrConfigPath = ibkrConfigPath;
        this.ibkrImporter = Objects.requireNonNull(ibkrImporter, "ibkrImporter");
        this.securityConfigs = Objects.requireNonNull(securityConfigs, "securityConfigs");
        this.executableDividendMethods = Set.copyOf(
                Objects.requireNonNull(executableDividendMethods, "executableDividendMethods"));
        this.securitySupportResolver = Objects.requireNonNull(securitySupportResolver, "securitySupportResolver");
        this.sourceConsolidator = Objects.requireNonNull(sourceConsolidator, "sourceConsolidator");
    }

    /**
     * Imports, resolves, and consolidates facts for one tax run using the shared account horizon.
     *
     * @param input shared tax-year input and opening state
     * @return consolidated facts and non-fatal review issues
     * @throws IOException when a configured provider source cannot be read
     * @throws InterruptedException when provider retrieval is interrupted
     */
    public SourceConsolidationResult assemble(TaxPipelineInput input)
            throws IOException, InterruptedException {
        TaxPipelineInput taxInput = Objects.requireNonNull(input, "input");
        SourceImportResult importResult = ibkrImporter.importSource(new IbkrImportInput(
                ibkrConfigPath,
                taxInput.getTaxYear().getStartDate().minusMonths(6),
                taxInput.getTaxYear().getEndDate().plusMonths(1)));
        SecuritySupportResult supportResult = securitySupportResolver.resolveDividendSupport(
                importResult.getSecurities(),
                securityConfigs,
                executableDividendMethods);
        return sourceConsolidator.consolidate(
                List.of(new PreparedSource(importResult, supportResult)),
                taxInput);
    }
}
