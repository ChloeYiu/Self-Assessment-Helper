package com.helper.calculations;

import com.helper.config.TaxYear;
import com.helper.income.IncomeResult;
import com.helper.income.IncomeType;
import com.helper.issue.Issue;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Combined calculated category results and non-fatal issues for one tax year. */
public class TaxCalculationResult {
    private final TaxYear taxYear;
    private final Map<IncomeType, IncomeResult> incomeResults;
    private final List<Issue> issues;

    public TaxCalculationResult(TaxYear taxYear, Map<IncomeType, IncomeResult> incomeResults, List<Issue> issues) {
        this.taxYear = Objects.requireNonNull(taxYear, "taxYear");
        this.incomeResults = validateAndCopyIncomeResults(incomeResults);
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }

    /**
     * Validates this result's category map while constructing the immutable aggregate.
     *
     * <p>This check belongs here because callers must not be able to create a result whose
     * map key names a different category from its enclosed income result.</p>
     */
    private static Map<IncomeType, IncomeResult> validateAndCopyIncomeResults(
            Map<IncomeType, IncomeResult> incomeResults
    ) {
        EnumMap<IncomeType, IncomeResult> results = new EnumMap<>(IncomeType.class);
        for (Map.Entry<IncomeType, IncomeResult> entry : Objects.requireNonNull(incomeResults, "incomeResults").entrySet()) {
            IncomeType incomeType = Objects.requireNonNull(entry.getKey(), "incomeType");
            IncomeResult incomeResult = Objects.requireNonNull(entry.getValue(), "incomeResult");
            if (incomeResult.getIncomeType() != incomeType) {
                throw new IllegalArgumentException("income result type must match its map key");
            }
            results.put(incomeType, incomeResult);
        }
        return Map.copyOf(results);
    }

    public TaxYear getTaxYear() { return taxYear; }
    public Map<IncomeType, IncomeResult> getIncomeResults() { return incomeResults; }
    public Optional<IncomeResult> findIncomeResult(IncomeType incomeType) {
        return Optional.ofNullable(incomeResults.get(Objects.requireNonNull(incomeType, "incomeType")));
    }
    public List<Issue> getIssues() { return issues; }
}
