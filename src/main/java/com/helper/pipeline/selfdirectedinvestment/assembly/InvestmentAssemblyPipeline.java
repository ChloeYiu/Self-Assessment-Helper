package com.helper.pipeline.selfdirectedinvestment.assembly;

import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigs;
import com.helper.pipeline.selfdirectedinvestment.TaxPipelineInput;
import com.helper.pipeline.selfdirectedinvestment.consolidation.PreparedSource;
import com.helper.pipeline.selfdirectedinvestment.consolidation.SourceConsolidationResult;
import com.helper.pipeline.selfdirectedinvestment.consolidation.SourceConsolidator;
import com.helper.pipeline.selfdirectedinvestment.importer.ibkr.IbkrImportInput;
import com.helper.pipeline.selfdirectedinvestment.importer.ibkr.IbkrImporter;
import com.helper.pipeline.selfdirectedinvestment.importer.SourceImportResult;
import com.helper.pipeline.selfdirectedinvestment.support.SecuritySupportResolver;
import com.helper.pipeline.selfdirectedinvestment.support.SecuritySupportResult;
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
     * Creates the initial assembly pipeline for one selected IBKR configuration.
     *
     * @param ibkrConfigPath selected account's local IBKR Flex configuration
     * @throws IOException when the fixed security configuration cannot be read
     */
    public InvestmentAssemblyPipeline(Path ibkrConfigPath) throws IOException {
        this(
                Objects.requireNonNull(ibkrConfigPath, "ibkrConfigPath"),
                new IbkrImporter(),
                SecurityConfigs.load(),
                Set.of(DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND),
                new SecuritySupportResolver(),
                new SourceConsolidator());
    }

    /**
     * Creates the investment-assembly coordinator for one configured IBKR source.
     *
     * @param ibkrConfigPath IBKR Flex configuration path
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
        this.ibkrConfigPath = Objects.requireNonNull(ibkrConfigPath, "ibkrConfigPath");
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
