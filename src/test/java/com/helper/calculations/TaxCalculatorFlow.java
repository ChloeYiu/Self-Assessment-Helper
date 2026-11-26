package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipelineType;
import com.helper.pipeline.managedinvestment.ManagedInvestmentPipelineInput;
import com.helper.support.registry.Registry;
import com.helper.support.registry.RegistryKeys;
import com.helper.util.pdf.Pdf;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;

/** Manual local flow for running the current top-level pipelines through {@link TaxCalculator}. */
public class TaxCalculatorFlow {
    private static final Path JPMPI_TAX_PACK_PATH = Path.of("files/jpmci/tax-pack.pdf");

    public static void main(String[] args) throws Exception {
        TaxYear taxYear = parseTaxYear(args);
        Registry registry = new Registry();
        registry.put(
                RegistryKeys.MANAGED_INVESTMENT_PIPELINE_INPUT,
                new ManagedInvestmentPipelineInput(List.of(new Pdf(JPMPI_TAX_PACK_PATH))));
        TaxCalculationResult result = new TaxCalculator().run(
                new TaxCalculationRequest(
                        taxYear,
                        EnumSet.of(
                                TaxPipelineType.SELF_DIRECTED_INVESTMENT,
                                TaxPipelineType.MANAGED_INVESTMENT)),
                registry);
        printResult(result);
    }

    private static TaxYear parseTaxYear(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: TaxCalculatorFlow <tax-year-start-year>");
        }
        return TaxYear.of(Integer.parseInt(args[0]));
    }

    private static void printResult(TaxCalculationResult result) {
        System.out.println("taxYear=" + result.getTaxYear().getStartYear()
                + "/" + (result.getTaxYear().getStartYear() + 1));
        for (IncomeResult incomeResult : result.getIncomeResults().values()) {
            System.out.println("incomeType=" + incomeResult.getIncomeType()
                    + " grossIncome=" + incomeResult.getGrossIncome()
                    + " adjustedIncome=" + incomeResult.getAdjustedIncome()
                    + " artifacts=" + incomeResult.getArtifacts().size());
        }
        System.out.println("issues=" + result.getIssues().size());
        for (Issue issue : result.getIssues()) {
            System.out.println("issue=" + issue.getCode()
                    + " severity=" + issue.getSeverity()
                    + " source=" + issue.getSource()
                    + " attributes=" + issue.getAttributes());
        }
    }
}
