package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.issue.Issue;
import com.helper.pipeline.TaxPipelineType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

/** Manual live flow for running one selected top-level pipeline through {@link TaxCalculator}. */
public class TaxCalculatorFlow {
    public static void main(String[] args) throws Exception {
        TaxYear taxYear = parseTaxYear(args);
        TaxCalculator calculator = new TaxCalculator();
        TaxPipelineType pipelineType = selectPipelineType(calculator.getAvailablePipelineTypes());
        TaxCalculationResult result = calculator.run(
                new TaxCalculationRequest(taxYear, Set.of(pipelineType)));

        printResult(result);
    }

    private static TaxYear parseTaxYear(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Usage: TaxCalculatorFlow <tax-year-start-year>");
        }
        return TaxYear.of(Integer.parseInt(args[0]));
    }

    private static TaxPipelineType selectPipelineType(Set<TaxPipelineType> availablePipelineTypes) {
        List<TaxPipelineType> pipelineTypes = new ArrayList<>(availablePipelineTypes);
        pipelineTypes.sort(Enum::compareTo);
        System.out.println("Select a tax pipeline:");
        for (int index = 0; index < pipelineTypes.size(); index++) {
            System.out.println((index + 1) + ". " + pipelineTypes.get(index));
        }

        Scanner scanner = new Scanner(System.in);
        int selection = Integer.parseInt(scanner.nextLine());
        if (selection < 1 || selection > pipelineTypes.size()) {
            throw new IllegalArgumentException("tax pipeline selection is out of range: " + selection);
        }
        return pipelineTypes.get(selection - 1);
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
