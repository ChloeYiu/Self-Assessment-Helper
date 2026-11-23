package com.helper.pipeline.selfdirectedinvestment.support;

import com.helper.income.config.SecurityConfig;
import com.helper.income.implementation.Security;
import com.helper.issue.Issue;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Dividend-support decisions for securities imported during one source run.
 */
public class SecuritySupportResult {
    private final List<Security> supportedDividendSecurities;
    private final Map<String, SecurityConfig> supportedDividendConfigsByIsin;
    private final List<Issue> issues;

    /**
     * Creates support decisions and review issues for imported securities.
     */
    public SecuritySupportResult(
            List<Security> supportedDividendSecurities,
            Map<String, SecurityConfig> supportedDividendConfigsByIsin,
            List<Issue> issues
    ) {
        this.supportedDividendSecurities = List.copyOf(
                Objects.requireNonNull(supportedDividendSecurities, "supportedDividendSecurities"));
        this.supportedDividendConfigsByIsin = Map.copyOf(
                Objects.requireNonNull(supportedDividendConfigsByIsin, "supportedDividendConfigsByIsin"));
        this.issues = List.copyOf(Objects.requireNonNull(issues, "issues"));
    }

    /** Returns securities with an executable configured dividend method. */
    public List<Security> getSupportedDividendSecurities() {
        return supportedDividendSecurities;
    }

    /** Returns the executable dividend configuration for a security when present. */
    public Optional<SecurityConfig> findSupportedDividendConfig(Security security) {
        return Optional.ofNullable(supportedDividendConfigsByIsin.get(
                Objects.requireNonNull(security, "security").getIdentifier()));
    }

    /** Returns review issues for unsupported or unconfigured securities. */
    public List<Issue> getIssues() {
        return issues;
    }
}
