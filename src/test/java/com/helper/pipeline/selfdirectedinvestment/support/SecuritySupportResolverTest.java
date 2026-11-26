package com.helper.pipeline.selfdirectedinvestment.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.helper.config.CurrencyCode;
import com.helper.income.config.CapitalGainCalculationMethod;
import com.helper.income.config.DividendCalculationMethod;
import com.helper.income.config.SecurityConfig;
import com.helper.income.config.SecurityConfigEnums.CapitalGainCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.DividendCalculationMethodType;
import com.helper.income.config.SecurityConfigEnums.EriReportFormat;
import com.helper.income.config.SecurityConfigs;
import com.helper.income.implementation.Security;
import com.helper.pipeline.selfdirectedinvestment.dividend.DividendIssueCode;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class SecuritySupportResolverTest {
    @Test
    public void resolveDividendSupport_returnsConfiguredSecurityAndReportsMissingConfiguration() {
        Security supportedSecurity = security("IE00B44Z5B48", "ACWI");
        Security missingConfigSecurity = security("IE00MISSING", "MISSING");
        SecurityConfigs configs = new SecurityConfigs(List.of(config("IE00B44Z5B48")));

        SecuritySupportResult result = new SecuritySupportResolver().resolveDividendSupport(
                List.of(supportedSecurity, missingConfigSecurity, missingConfigSecurity),
                configs,
                Set.of(DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND));

        assertEquals(List.of(supportedSecurity), result.getSupportedDividendSecurities());
        assertEquals(DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND,
                result.findSupportedDividendConfig(supportedSecurity).orElseThrow()
                        .getDividendCalculationMethod().getType());
        assertEquals(1, result.getIssues().size());
        assertEquals(SecuritySupportIssueCode.MISSING_CONFIGURATION, result.getIssues().get(0).getCode());
    }

    @Test
    public void resolveDividendSupport_reportsConfiguredUnsupportedMethod() {
        Security security = security("IE00UNSUPPORTED", "UNSUPPORTED");
        SecurityConfigs configs = new SecurityConfigs(List.of(config(
                security.getIdentifier(),
                DividendCalculationMethodType.UNSUPPORTED)));

        SecuritySupportResult result = new SecuritySupportResolver().resolveDividendSupport(
                List.of(security),
                configs,
                Set.of(DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND));

        assertTrue(result.getSupportedDividendSecurities().isEmpty());
        assertEquals(SecuritySupportIssueCode.UNSUPPORTED_DIVIDEND_METHOD, result.getIssues().get(0).getCode());
    }

    @Test
    public void resolveDividendSupport_reportsConfiguredMethodThatIsNotExecutable() {
        Security security = security("IE00UNIMPLEMENTED", "UNIMPLEMENTED");
        SecurityConfigs configs = new SecurityConfigs(List.of(config(security.getIdentifier())));

        SecuritySupportResult result = new SecuritySupportResolver().resolveDividendSupport(
                List.of(security), configs, Set.of());

        assertTrue(result.getSupportedDividendSecurities().isEmpty());
        assertEquals(DividendIssueCode.UNIMPLEMENTED_METHOD, result.getIssues().get(0).getCode());
    }

    private static Security security(String isin, String ticker) {
        return new Security(isin, ticker, ticker + " fund", CurrencyCode.USD);
    }

    static SecurityConfig config(String isin) {
        return config(isin, DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND);
    }

    private static SecurityConfig config(String isin, DividendCalculationMethodType dividendMethod) {
        return new SecurityConfig(
                isin,
                new DividendCalculationMethod(
                        dividendMethod,
                        dividendMethod == DividendCalculationMethodType.NON_UK_ACCUMULATING_FUND
                                ? EriReportFormat.SSGA_SPDR_UK_REPORTABLE_INCOME
                                : null),
                new CapitalGainCalculationMethod(CapitalGainCalculationMethodType.STANDARD_SECURITY_POOLING));
    }
}
