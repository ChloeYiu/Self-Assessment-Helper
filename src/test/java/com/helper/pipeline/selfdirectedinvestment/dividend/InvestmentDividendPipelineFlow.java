package com.helper.pipeline.selfdirectedinvestment.dividend;

import com.helper.config.TaxYear;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigs;
import com.helper.income.implementation.dividend.Dividend;
import com.helper.income.implementation.dividend.model.DividendResult;
import com.helper.issue.Issue;
import com.helper.pipeline.selfdirectedinvestment.TaxPipelineInput;
import com.helper.pipeline.selfdirectedinvestment.assembly.InvestmentAssemblyPipeline;
import com.helper.pipeline.selfdirectedinvestment.consolidation.SourceConsolidationResult;
import com.helper.pipeline.selfdirectedinvestment.consolidation.SourceConsolidator;
import com.helper.pipeline.selfdirectedinvestment.importer.ibkr.IbkrImporter;
import com.helper.pipeline.selfdirectedinvestment.support.SecuritySupportResolver;
import com.helper.support.fx.FxRateProvider;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Manual live flow for inspecting investment dividend preparation for one UK tax year.
 *
 * <p>Uses {@code config/ibkr-flex.properties} and {@code config/security-configs.yaml}.
 * Pass the tax-year start year, for example {@code 2025} for 2025/26. The flow fetches
 * IBKR source facts, HMRC FX documents, and the configured issuer ERI report. It prints
 * prepared dividend amounts and all source and preparation issues.</p>
 */
public class InvestmentDividendPipelineFlow {
    private static final Path IBKR_CONFIG_PATH = Path.of("config/ibkr-flex.properties");
    private static final Path SECURITY_CONFIG_PATH = Path.of("config/security-configs.yaml");

    public static void main(String[] args) throws Exception {
        TaxYear taxYear = parseTaxYear(args);
        requireConfigFiles();
        Registry registry = createRegistry(taxYear);
        SourceConsolidationResult sourceResult = createAssemblyPipeline().assemble(
                new TaxPipelineInput(taxYear, List.of()));
        InvestmentDividendResult dividendResult = new InvestmentDividendPipeline(registry)
                .prepare(sourceResult);

        printSummary(taxYear, sourceResult, dividendResult);
    }

    private static TaxYear parseTaxYear(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: InvestmentDividendPipelineFlow <tax-year-start-year>");
        }
        return TaxYear.of(Integer.parseInt(args[0]));
    }

    private static void requireConfigFiles() {
        if (!Files.exists(IBKR_CONFIG_PATH)) {
            throw new IllegalStateException("Missing IBKR Flex config at " + IBKR_CONFIG_PATH);
        }
        if (!Files.exists(SECURITY_CONFIG_PATH)) {
            throw new IllegalStateException("Missing security config at " + SECURITY_CONFIG_PATH);
        }
    }

    private static Registry createRegistry(TaxYear taxYear) throws Exception {
        FxRateProvider fxRateProvider = new FxRateProvider();
        Registry registry = new Registry();
        registry.put(
                RegistryKeys.MONTHLY_FX_RATE_DOCUMENT,
                fxRateProvider.fetchMonthlyRateDocument(taxYear));
        registry.put(
                RegistryKeys.YEARLY_FX_RATE_DOCUMENT,
                fxRateProvider.fetchYearlyRateDocument(taxYear));
        return registry;
    }

    private static InvestmentAssemblyPipeline createAssemblyPipeline() throws Exception {
        return new InvestmentAssemblyPipeline(
                IBKR_CONFIG_PATH,
                new IbkrImporter(),
                SecurityConfigs.load(SECURITY_CONFIG_PATH),
                Set.of(DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND),
                new SecuritySupportResolver(),
                new SourceConsolidator());
    }

    private static void printSummary(
            TaxYear taxYear,
            SourceConsolidationResult sourceResult,
            InvestmentDividendResult dividendResult
    ) {
        System.out.println("taxYear=" + taxYear.getStartYear() + "/" + (taxYear.getStartYear() + 1));
        System.out.println("supportedSecurities=" + sourceResult.securities().size());
        System.out.println("preparedDividends=" + dividendResult.dividendSources().size());

        for (Dividend dividend : dividendResult.dividendSources()) {
            DividendResult result = dividend.calculateDividendResult();
            System.out.println("dividendSource=" + dividend.getClass().getSimpleName()
                    + " amountGbp=" + result.getDividendAmount()
                    + " artifacts=" + result.getArtifacts().size());
        }

        List<Issue> issues = new ArrayList<>(sourceResult.issues());
        issues.addAll(dividendResult.issues());
        System.out.println("issues=" + issues.size());
        for (Issue issue : issues) {
            System.out.println("issue=" + issue.getCode()
                    + " severity=" + issue.getSeverity()
                    + " source=" + issue.getSource()
                    + " attributes=" + issue.getAttributes());
        }
    }
}
