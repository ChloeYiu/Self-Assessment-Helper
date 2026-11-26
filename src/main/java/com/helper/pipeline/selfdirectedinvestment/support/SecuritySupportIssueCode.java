package com.helper.pipeline.selfdirectedinvestment.support;

import com.helper.issue.IssueCode;

/** Issues produced while determining whether a security has tax-method support. */
public enum SecuritySupportIssueCode implements IssueCode {
    MISSING_CONFIGURATION("No security configuration exists for this ISIN"),
    UNSUPPORTED_DIVIDEND_METHOD("The configured dividend method is unsupported");

    private final String debugReason;

    SecuritySupportIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    @Override
    public String getDebugReason() {
        return debugReason;
    }
}
