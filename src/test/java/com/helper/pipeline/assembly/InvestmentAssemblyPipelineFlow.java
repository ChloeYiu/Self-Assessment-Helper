package com.helper.pipeline.assembly;

import com.helper.config.TaxYear;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigs;
import com.helper.income.implementation.Trade;
import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipelineInput;
import com.helper.pipeline.consolidation.ConsolidatedSecurityFacts;
import com.helper.pipeline.consolidation.SourceConsolidationResult;
import com.helper.pipeline.consolidation.SourceConsolidator;
import com.helper.pipeline.importer.ibkr.IbkrImporter;
import com.helper.pipeline.support.SecuritySupportResolver;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Manual live flow for inspecting the IBKR investment-assembly result for one UK tax year.
 *
 * <p>Uses {@code config/ibkr-flex.properties} and {@code config/security-configs.yaml}.
 * Pass the tax-year start year, for example {@code 2024} for 2024/25. The flow requests
 * the shared account horizon from six months before the tax-year start through one month
 * after the tax-year end. It supplies no opening holding or capital-gain snapshots, so it
 * stops after investment assembly and does not calculate a tax result.</p>
 */
public class InvestmentAssemblyPipelineFlow {
    private static final Path IBKR_CONFIG_PATH = Path.of("config/ibkr-flex.properties");
    private static final Path SECURITY_CONFIG_PATH = Path.of("config/security-configs.yaml");

    public static void main(String[] args) throws Exception {
        TaxYear taxYear = parseTaxYear(args);
        requireConfigFiles();

        InvestmentAssemblyPipeline pipeline = new InvestmentAssemblyPipeline(
                IBKR_CONFIG_PATH,
                new IbkrImporter(),
                SecurityConfigs.load(SECURITY_CONFIG_PATH),
                Set.of(DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND),
                new SecuritySupportResolver(),
                new SourceConsolidator());
        SourceConsolidationResult result = pipeline.assemble(new TaxPipelineInput(
                taxYear,
                List.of()));

        printSummary(taxYear, result);
    }

    private static TaxYear parseTaxYear(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: InvestmentAssemblyPipelineFlow <tax-year-start-year>");
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

    private static void printSummary(TaxYear taxYear, SourceConsolidationResult result) {
        LocalDate requestedStartDate = taxYear.getStartDate().minusMonths(6);
        LocalDate requestedEndDate = taxYear.getEndDate().plusMonths(1);
        System.out.println("taxYear=" + taxYear.getStartYear() + "/" + (taxYear.getStartYear() + 1));
        System.out.println("requestedStartDate=" + requestedStartDate);
        System.out.println("requestedEndDate=" + requestedEndDate);
        System.out.println("openingHoldingSnapshots=derived from imported positions");
        System.out.println("openingCapitalGainSnapshots=0");
        System.out.println("supportedSecurities=" + result.securities().size());

        for (ConsolidatedSecurityFacts security : result.securities()) {
            System.out.println("security=" + security.security().getIdentifier()
                    + " ticker=" + security.security().getTicker()
                    + " trades=" + security.trades().size()
                    + " holdingSnapshot=" + security.holdingSnapshot()
                    + " carryForwardSnapshot=" + security.carryForwardSnapshot());
            for (Trade trade : security.trades()) {
                System.out.println("trade=" + trade.getTradeId()
                        + " date=" + trade.getTransactionDate()
                        + " type=" + trade.getTradeType()
                        + " quantity=" + trade.getQuantity()
                        + " grossAmountGbp=" + trade.getGrossAmountGbp()
                        + " feeGbp=" + trade.getFeeGbp());
            }
        }

        System.out.println("issues=" + result.issues().size());
        for (Issue issue : result.issues()) {
            System.out.println("issue=" + issue.getCode()
                    + " source=" + issue.getSource()
                    + " attributes=" + issue.getAttributes());
        }
    }
}
