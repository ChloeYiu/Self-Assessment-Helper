package com.helper.pipeline.security;

import com.helper.income.config.SecurityConfig;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigs;
import com.helper.income.implementation.Security;
import com.helper.income.issue.IncomeIssue;
import com.helper.income.issue.IncomeIssueCode;
import com.helper.income.issue.IncomeIssueSeverity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Resolves whether imported securities have a configured and executable dividend method.
 */
public class SecuritySupportResolver {
    private static final String ISSUE_SOURCE = "security-support";

    /**
     * Resolves executable dividend support without performing provider I/O.
     *
     * @param securities securities detected during one provider import
     * @param securityConfigs configured tax treatment by ISIN
     * @param executableDividendMethods methods implemented by the current application path
     * @return supported securities/configurations and review issues for the remainder
     */
    public SecuritySupportResult resolveDividendSupport(
            List<Security> securities,
            SecurityConfigs securityConfigs,
            Set<DividendCalculationMethodType> executableDividendMethods
    ) {
        List<Security> importedSecurities = List.copyOf(Objects.requireNonNull(securities, "securities"));
        SecurityConfigs configs = Objects.requireNonNull(securityConfigs, "securityConfigs");
        Set<DividendCalculationMethodType> executableMethods = Set.copyOf(
                Objects.requireNonNull(executableDividendMethods, "executableDividendMethods"));

        List<Security> supportedSecurities = new ArrayList<>();
        Map<String, SecurityConfig> supportedConfigs = new LinkedHashMap<>();
        List<IncomeIssue> issues = new ArrayList<>();
        Set<String> resolvedIsins = new LinkedHashSet<>();

        for (Security security : importedSecurities) {
            // Resolve each ISIN once so repeated imported rows do not duplicate decisions or issues.
            if (!resolvedIsins.add(security.getIdentifier())) {
                continue;
            }

            Optional<SecurityConfig> config = configs.findByIsin(security.getIdentifier());
            if (config.isEmpty()) {
                issues.add(issue(IncomeIssueCode.MISSING_SECURITY_CONFIGURATION, security));
                continue;
            }

            DividendCalculationMethodType method = config.get().getDividendCalculationMethod().getType();
            if (method == DividendCalculationMethodType.UNSUPPORTED) {
                issues.add(issue(IncomeIssueCode.UNSUPPORTED_DIVIDEND_METHOD, security));
                continue;
            }
            if (!executableMethods.contains(method)) {
                issues.add(issue(IncomeIssueCode.UNIMPLEMENTED_DIVIDEND_METHOD, security));
                continue;
            }

            supportedSecurities.add(security);
            supportedConfigs.put(security.getIdentifier(), config.get());
        }

        return new SecuritySupportResult(supportedSecurities, supportedConfigs, issues);
    }

    private IncomeIssue issue(IncomeIssueCode code, Security security) {
        return new IncomeIssue(
                code,
                IncomeIssueSeverity.WARNING,
                ISSUE_SOURCE,
                Map.of(
                        "isin", security.getIdentifier(),
                        "ticker", security.getTicker(),
                        "name", security.getName()));
    }
}
