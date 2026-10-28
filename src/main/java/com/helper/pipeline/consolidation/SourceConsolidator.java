package com.helper.pipeline.consolidation;

import com.helper.income.config.SecurityConfig;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.pipeline.TaxPipelineInput;
import com.helper.pipeline.importer.SourceImportResult;
import com.helper.pipeline.support.SecuritySupportResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Groups supported facts from imported sources into per-security calculation inputs.
 */
public class SourceConsolidator {
    /**
     * Consolidates supported securities by ISIN, pairing each with its configured treatment,
     * matching imported trades, and supplied opening snapshots.
     *
     * <p>This method does not deduplicate overlapping imports, resolve conflicting security
     * metadata, or decide whether source coverage is sufficient. Each supported security can
     * receive at most one holding snapshot and one carry-forward snapshot.</p>
     *
     * @param preparedSources provider facts paired with their support decisions
     * @param taxInput tax year and opening state for this run
     * @return one consolidated input for each supported ISIN
     * @throws NullPointerException if an input or required paired value is null
     * @throws IllegalArgumentException if a supported security was not imported by its paired source
     */
    public SourceConsolidationResult consolidate(
            List<PreparedSource> preparedSources,
            TaxPipelineInput taxInput
    ) {
        Objects.requireNonNull(preparedSources, "preparedSources");
        Objects.requireNonNull(taxInput, "taxInput");
        Map<String, ConsolidatedSecurityFacts.Builder> buildersByIsin = new LinkedHashMap<>();
        Map<IssueKey, Issue> issuesByKey = new LinkedHashMap<>();

        for (PreparedSource preparedSource : preparedSources) {
            Objects.requireNonNull(preparedSource, "preparedSource");
            addIssues(preparedSource.sourceImportResult().getIssues(), issuesByKey);
            addIssues(preparedSource.securitySupportResult().getIssues(), issuesByKey);
            addSource(preparedSource, buildersByIsin);
        }

        addHoldingSnapshots(taxInput.getOpeningHoldingSnapshots(), buildersByIsin);
        addCarryForwardSnapshots(taxInput.getOpeningCapitalGainSnapshots(), buildersByIsin);

        List<ConsolidatedSecurityFacts> securities = buildersByIsin.values().stream()
                .map(ConsolidatedSecurityFacts.Builder::build)
                .toList();
        return new SourceConsolidationResult(
                taxInput.getTaxYear(),
                securities,
                List.copyOf(issuesByKey.values()));
    }

    private void addIssues(
            List<Issue> issues,
            Map<IssueKey, Issue> issuesByKey
    ) {
        for (Issue issue : issues) {
            Objects.requireNonNull(issue, "issue");
            issuesByKey.putIfAbsent(
                    new IssueKey(issue.getCode(), issue.getSource(), issue.getAttributes()),
                    issue);
        }
    }

    private void addSource(
            PreparedSource preparedSource,
            Map<String, ConsolidatedSecurityFacts.Builder> buildersByIsin
    ) {
        SourceImportResult importResult = preparedSource.sourceImportResult();
        SecuritySupportResult supportResult = preparedSource.securitySupportResult();

        for (Security security : supportResult.getSupportedDividendSecurities()) {
            String isin = security.getIdentifier();
            SecurityConfig config = supportResult
                    .findSupportedDividendConfig(security)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Supported security has no configuration: " + isin));
            ConsolidatedSecurityFacts.Builder builder = buildersByIsin.computeIfAbsent(
                    isin,
                    ignored -> new ConsolidatedSecurityFacts.Builder(security, config));
            addMatchingTrades(importResult.getTrades(), isin, builder);
        }
    }

    private void addMatchingTrades(
            List<Trade> trades,
            String isin,
            ConsolidatedSecurityFacts.Builder destination
    ) {
        for (Trade trade : trades) {
            if (trade.getSecurity().getIdentifier().equals(isin)) {
                destination.addTrade(trade);
            }
        }
    }

    private void addHoldingSnapshots(
            List<SecurityHoldingSnapshot> snapshots,
            Map<String, ConsolidatedSecurityFacts.Builder> buildersByIsin
    ) {
        for (SecurityHoldingSnapshot snapshot : snapshots) {
            ConsolidatedSecurityFacts.Builder builder = buildersByIsin.get(
                    snapshot.getSecurity().getIdentifier());
            if (builder != null) {
                builder.setHoldingSnapshot(snapshot);
            }
        }
    }

    private void addCarryForwardSnapshots(
            List<CarryForwardSnapshot> snapshots,
            Map<String, ConsolidatedSecurityFacts.Builder> buildersByIsin
    ) {
        for (CarryForwardSnapshot snapshot : snapshots) {
            ConsolidatedSecurityFacts.Builder builder = buildersByIsin.get(
                    snapshot.getSecurity().getIdentifier());
            if (builder != null) {
                builder.setCarryForwardSnapshot(snapshot);
            }
        }
    }

    private record IssueKey(
            IssueCode code,
            String source,
            Map<String, String> attributes
    ) {
    }
}
