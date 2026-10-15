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
     * metadata, select an opening holding, or decide whether source coverage is sufficient.</p>
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
        Map<String, ConsolidatedSecurity.Builder> buildersByIsin = new LinkedHashMap<>();
        Map<IssueKey, Issue> issuesByKey = new LinkedHashMap<>();

        for (PreparedSource preparedSource : preparedSources) {
            PreparedSource source = Objects.requireNonNull(preparedSource, "preparedSource");
            addIssues(source.sourceImportResult().getIssues(), issuesByKey);
            addIssues(source.securitySupportResult().getIssues(), issuesByKey);
            addSource(source, buildersByIsin);
        }

        addHoldingSnapshots(taxInput.getOpeningHoldingSnapshots(), buildersByIsin);
        addCarryForwardSnapshots(taxInput.getOpeningCapitalGainSnapshots(), buildersByIsin);

        List<ConsolidatedSecurity> securities = buildersByIsin.values().stream()
                .map(ConsolidatedSecurity.Builder::build)
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
            Issue value = Objects.requireNonNull(issue, "issue");
            issuesByKey.putIfAbsent(
                    new IssueKey(value.getCode(), value.getSource(), value.getAttributes()),
                    value);
        }
    }

    private void addSource(
            PreparedSource preparedSource,
            Map<String, ConsolidatedSecurity.Builder> buildersByIsin
    ) {
        SourceImportResult importResult = preparedSource.sourceImportResult();
        SecuritySupportResult supportResult = preparedSource.securitySupportResult();

        for (Security security : supportResult.getSupportedDividendSecurities()) {
            String isin = security.getIdentifier();
            SecurityConfig config = supportResult
                    .findSupportedDividendConfig(security)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Supported security has no configuration: " + isin));
            ConsolidatedSecurity.Builder builder = buildersByIsin.computeIfAbsent(
                    isin,
                    ignored -> new ConsolidatedSecurity.Builder(security, config));
            addMatchingTrades(importResult.getTrades(), isin, builder);
        }
    }

    private void addMatchingTrades(
            List<Trade> trades,
            String isin,
            ConsolidatedSecurity.Builder destination
    ) {
        for (Trade trade : trades) {
            if (trade.getSecurity().getIdentifier().equals(isin)) {
                destination.addTrade(trade);
            }
        }
    }

    private void addHoldingSnapshots(
            List<SecurityHoldingSnapshot> snapshots,
            Map<String, ConsolidatedSecurity.Builder> buildersByIsin
    ) {
        for (SecurityHoldingSnapshot snapshot : snapshots) {
            ConsolidatedSecurity.Builder builder = buildersByIsin.get(
                    snapshot.getSecurity().getIdentifier());
            if (builder != null) {
                builder.addHoldingSnapshot(snapshot);
            }
        }
    }

    private void addCarryForwardSnapshots(
            List<CarryForwardSnapshot> snapshots,
            Map<String, ConsolidatedSecurity.Builder> buildersByIsin
    ) {
        for (CarryForwardSnapshot snapshot : snapshots) {
            ConsolidatedSecurity.Builder builder = buildersByIsin.get(
                    snapshot.getSecurity().getIdentifier());
            if (builder != null) {
                builder.addCarryForwardSnapshot(snapshot);
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
