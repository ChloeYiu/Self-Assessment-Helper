package com.helper.pipeline.selfdirectedinvestment.consolidation;

import com.helper.income.config.SecurityConfig;
import com.helper.income.implementation.Security;
import com.helper.income.implementation.Trade;
import com.helper.income.implementation.capitalgain.model.CarryForwardSnapshot;
import com.helper.income.implementation.dividend.model.SecurityHoldingSnapshot;
import com.helper.issue.Issue;
import com.helper.issue.IssueCode;
import com.helper.issue.IssueSeverity;
import com.helper.pipeline.selfdirectedinvestment.TaxPipelineInput;
import com.helper.pipeline.selfdirectedinvestment.importer.Position;
import com.helper.pipeline.selfdirectedinvestment.importer.SourceImportResult;
import com.helper.pipeline.selfdirectedinvestment.support.SecuritySupportResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Groups supported facts from imported sources into per-security calculation inputs.
 */
public class SourceConsolidator {
    /**
     * Consolidates supported securities by ISIN, pairing each with its configured treatment,
     * matching imported trades, and imported opening positions.
     *
     * <p>This method does not deduplicate overlapping imports, resolve conflicting security
     * metadata, or decide whether source coverage is sufficient. Each supported security can
     * receive at most one holding snapshot and one carry-forward snapshot.</p>
     *
     * @param preparedSources provider facts paired with their support decisions
     * @param taxInput tax year and capital-gain carry-forward state for this run
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
        Map<String, HoldingSnapshotConsolidator> holdingsByIsin = new LinkedHashMap<>();
        Map<IssueKey, Issue> issuesByKey = new LinkedHashMap<>();
        LocalDate holdingCutoffDate = taxInput.getTaxYear().getStartDate().minusMonths(6);

        for (PreparedSource preparedSource : preparedSources) {
            Objects.requireNonNull(preparedSource, "preparedSource");
            addIssues(preparedSource.sourceImportResult().getIssues(), issuesByKey);
            addIssues(preparedSource.securitySupportResult().getIssues(), issuesByKey);
            addSource(preparedSource, holdingCutoffDate, buildersByIsin, holdingsByIsin);
        }

        addHoldingSnapshots(holdingsByIsin, buildersByIsin, issuesByKey);
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
            LocalDate holdingCutoffDate,
            Map<String, ConsolidatedSecurityFacts.Builder> buildersByIsin,
            Map<String, HoldingSnapshotConsolidator> holdingsByIsin
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
            addMatchingPositions(
                    importResult.getPositions(),
                    security,
                    isin,
                    holdingCutoffDate,
                    holdingsByIsin);
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

    private void addMatchingPositions(
            List<Position> positions,
            Security security,
            String isin,
            LocalDate holdingCutoffDate,
            Map<String, HoldingSnapshotConsolidator> holdingsByIsin
    ) {
        HoldingSnapshotConsolidator holding = holdingsByIsin.computeIfAbsent(
                isin, ignored -> new HoldingSnapshotConsolidator(security, holdingCutoffDate));
        for (Position position : positions) {
            if (position.isin().equals(isin)) {
                holding.add(position);
            }
        }
    }

    private void addHoldingSnapshots(
            Map<String, HoldingSnapshotConsolidator> holdingsByIsin,
            Map<String, ConsolidatedSecurityFacts.Builder> buildersByIsin,
            Map<IssueKey, Issue> issuesByKey
    ) {
        for (Map.Entry<String, HoldingSnapshotConsolidator> entry : holdingsByIsin.entrySet()) {
            ConsolidatedSecurityFacts.Builder builder = buildersByIsin.get(entry.getKey());
            if (builder != null) {
                builder.setHoldingSnapshot(entry.getValue().toSnapshot());
                if (entry.getValue().isZeroQuantity()) {
                    Issue issue = zeroHoldingIssue(entry.getKey(), entry.getValue().cutoffDate());
                    issuesByKey.putIfAbsent(
                            new IssueKey(issue.getCode(), issue.getSource(), issue.getAttributes()),
                            issue);
                }
            }
        }
    }

    private static Issue zeroHoldingIssue(String isin, LocalDate cutoffDate) {
        return new Issue(
                IssueCode.ZERO_DIVIDEND_OPENING_HOLDING,
                IssueSeverity.INFO,
                "source-consolidation",
                Map.of("isin", isin, "cutoffDate", cutoffDate.toString()));
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

    private record SourceAccountKey(String source, String accountId) {
    }

    private static final class HoldingSnapshotConsolidator {
        private final Security security;
        private final LocalDate cutoffDate;
        private final Set<SourceAccountKey> sourceAccounts = new HashSet<>();
        private BigDecimal quantity = BigDecimal.ZERO;

        private HoldingSnapshotConsolidator(Security security, LocalDate cutoffDate) {
            this.security = Objects.requireNonNull(security, "security");
            this.cutoffDate = Objects.requireNonNull(cutoffDate, "cutoffDate");
        }

        private void add(Position position) {
            if (!cutoffDate.equals(position.positionDate())) {
                throw new IllegalArgumentException(
                        "position date must match shared holding cutoff: " + cutoffDate);
            }
            if (!sourceAccounts.add(new SourceAccountKey(position.source(), position.accountId()))) {
                throw new IllegalArgumentException(
                        "duplicate source account position for security: " + position.isin());
            }
            quantity = quantity.add(position.quantity());
        }

        private boolean isZeroQuantity() {
            return quantity.compareTo(BigDecimal.ZERO) == 0;
        }

        private LocalDate cutoffDate() {
            return cutoffDate;
        }

        private SecurityHoldingSnapshot toSnapshot() {
            return new SecurityHoldingSnapshot(cutoffDate, security, quantity);
        }
    }
}
