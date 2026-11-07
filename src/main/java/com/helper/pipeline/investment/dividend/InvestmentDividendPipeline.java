package com.helper.pipeline.investment.dividend;

import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.implementation.dividend.Dividend;
import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.investment.consolidation.ConsolidatedSecurityFacts;
import com.helper.pipeline.investment.consolidation.SourceConsolidationResult;
import com.helper.support.registry.Registry;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Coordinates dividend-method pipelines for one investment tax calculation.
 *
 * <p>The completed contract will accept consolidated source facts, use a
 * {@link DividendMethodDispatcher}, and return one combined investment dividend result.</p>
 */
public class InvestmentDividendPipeline {
    private final DividendMethodDispatcher dispatcher;
    private final Registry registry;

    /**
     * Creates the investment dividend coordinator with its method dispatcher.
     *
     * @param registry prefetched shared run resources
     */
    public InvestmentDividendPipeline(Registry registry) {
        this(new DividendMethodDispatcher(), registry);
    }

    /** Creates the coordinator with explicit routing for package-level tests. */
    InvestmentDividendPipeline(DividendMethodDispatcher dispatcher, Registry registry) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * Prepares the combined investment dividend result from consolidated source facts.
     *
     * @param sourceConsolidationResult supported facts and source review issues for one run
     * @return prepared dividend sources and non-fatal preparation issues
     */
    public InvestmentDividendResult prepare(
            SourceConsolidationResult sourceConsolidationResult
    ) {
        SourceConsolidationResult sourceResult = Objects.requireNonNull(
                sourceConsolidationResult, "sourceConsolidationResult");
        Map<DividendCalculationMethodType, List<ConsolidatedSecurityFacts>> factsByMethod =
                new EnumMap<>(DividendCalculationMethodType.class);

        for (ConsolidatedSecurityFacts securityFacts : sourceResult.securities()) {
            DividendCalculationMethodType methodType = securityFacts.config()
                    .getDividendCalculationMethod()
                    .getType();
            factsByMethod.computeIfAbsent(methodType, ignored -> new ArrayList<>()).add(securityFacts);
        }

        List<Dividend> dividendSources = new ArrayList<>();
        List<Issue> issues = new ArrayList<>();

        for (Map.Entry<DividendCalculationMethodType, List<ConsolidatedSecurityFacts>> entry
                : factsByMethod.entrySet()) {
            DividendCalculationMethodType methodType = entry.getKey();
            List<ConsolidatedSecurityFacts> securityFacts = List.copyOf(entry.getValue());

            dispatcher.findPipeline(methodType).ifPresentOrElse(
                    pipeline -> {
                        DividendMethodResult dividendResult = pipeline.prepareDividendSources(
                                sourceResult.taxYear(), registry, securityFacts);
                        dividendSources.addAll(dividendResult.dividendSources());
                        issues.addAll(dividendResult.issues());
                    },
                    () -> addUnimplementedMethodIssues(securityFacts, methodType, issues));
        }

        return new InvestmentDividendResult(dividendSources, issues);
    }

    private static void addUnimplementedMethodIssues(
            List<ConsolidatedSecurityFacts> securityFacts,
            DividendCalculationMethodType methodType,
            List<Issue> issues
    ) {
        for (ConsolidatedSecurityFacts securityFact : securityFacts) {
            issues.add(unimplementedMethodIssue(securityFact, methodType));
        }
    }

    private static Issue unimplementedMethodIssue(
            ConsolidatedSecurityFacts securityFacts,
            DividendCalculationMethodType methodType
    ) {
        return new Issue(
                IssueCode.UNIMPLEMENTED_DIVIDEND_METHOD,
                IssueSeverity.WARNING,
                "investment-dividend",
                Map.of(
                        "isin", securityFacts.security().getIdentifier(),
                        "methodType", methodType.name()));
    }
}
