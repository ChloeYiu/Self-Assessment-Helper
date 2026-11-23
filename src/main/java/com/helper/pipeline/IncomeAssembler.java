package com.helper.pipeline;

import com.helper.calculations.TaxCalculationResult;
import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.income.IncomeType;
import com.helper.income.implementation.ForeignDividendIncome;
import com.helper.income.implementation.ForeignSavingsIncome;
import com.helper.income.implementation.UkDividendIncome;
import com.helper.income.implementation.UkSavingsIncome;
import com.helper.issue.Issue;
import com.helper.pipeline.selfdirectedinvestment.SelfDirectedInvestmentPipelineResult;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Builds the income aggregates for one tax calculation from registered pipeline results.
 *
 * <p>Each concrete pipeline-result type has one handler in {@link #RESULT_HANDLERS}. A handler
 * adds prepared sources to the appropriate income aggregate; it does not perform source
 * calculation or import data.</p>
 */
public final class IncomeAssembler {
    private static final Map<Class<? extends TaxPipelineResult>, ResultHandler> RESULT_HANDLERS = Map.of(
            SelfDirectedInvestmentPipelineResult.class, IncomeAssembler::addSelfDirectedInvestmentResult);

    private final UkDividendIncome ukDividendIncome;
    private final ForeignDividendIncome foreignDividendIncome;
    private final UkSavingsIncome ukSavingIncome;
    private final ForeignSavingsIncome foreignSavingIncome;
    private final List<Issue> issues = new ArrayList<>();

    /** Creates an assembler and its tax-year-specific income aggregates. */
    public IncomeAssembler(TaxYear taxYear) {
        Objects.requireNonNull(taxYear, "taxYear");
        this.ukDividendIncome = new UkDividendIncome(taxYear);
        this.foreignDividendIncome = new ForeignDividendIncome(taxYear);
        this.ukSavingIncome = new UkSavingsIncome(taxYear);
        this.foreignSavingIncome = new ForeignSavingsIncome(taxYear);
    }

    /** Adds one registered pipeline result to its matching income aggregates. */
    public void add(TaxPipelineResult result) {
        Objects.requireNonNull(result, "result");
        ResultHandler handler = RESULT_HANDLERS.get(result.getClass());
        if (handler == null) {
            throw new IllegalArgumentException("no income-assembly handler is registered for "
                    + result.getClass().getName());
        }
        handler.add(this, result);
        issues.addAll(result.issues());
    }

    /** Returns the one completed tax-calculation result assembled so far. */
    public TaxCalculationResult build() {
        Map<IncomeType, IncomeResult> incomeResults = new EnumMap<>(IncomeType.class);
        incomeResults.put(IncomeType.UK_DIVIDENDS, ukDividendIncome.calculateResult());
        incomeResults.put(IncomeType.FOREIGN_DIVIDENDS, foreignDividendIncome.calculateResult());
        incomeResults.put(IncomeType.UK_SAVINGS, ukSavingIncome.calculateResult());
        incomeResults.put(IncomeType.FOREIGN_SAVINGS, foreignSavingIncome.calculateResult());
        return new TaxCalculationResult(ukDividendIncome.getTaxYear(), incomeResults, issues);
    }

    private static void addSelfDirectedInvestmentResult(IncomeAssembler assembler, TaxPipelineResult result) {
        SelfDirectedInvestmentPipelineResult investmentResult = (SelfDirectedInvestmentPipelineResult) result;
        investmentResult.ukDividendSources().forEach(assembler.ukDividendIncome::addSource);
        investmentResult.foreignDividendSources().forEach(assembler.foreignDividendIncome::addSource);
    }

    @FunctionalInterface
    private interface ResultHandler {
        void add(IncomeAssembler assembler, TaxPipelineResult result);
    }
}
