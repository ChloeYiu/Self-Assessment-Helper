package com.helper.income.issue;

/** Stable machine-readable income issue codes. */
public enum IncomeIssueCode {
    MISSING_SECURITY_CONFIGURATION("No security configuration exists for this ISIN"),
    UNSUPPORTED_DIVIDEND_METHOD("The configured dividend method is unsupported"),
    UNIMPLEMENTED_DIVIDEND_METHOD("The configured dividend method is not executable in this run");

    private final String debugReason;

    IncomeIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    /** Returns the fixed explanation used when inspecting this issue code. */
    public String getDebugReason() {
        return debugReason;
    }
}
