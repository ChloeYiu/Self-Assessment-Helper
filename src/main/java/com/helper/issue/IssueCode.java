package com.helper.issue;

/** Stable machine-readable issue codes. */
public enum IssueCode {
    MISSING_SECURITY_CONFIGURATION("No security configuration exists for this ISIN"),
    UNSUPPORTED_DIVIDEND_METHOD("The configured dividend method is unsupported"),
    UNIMPLEMENTED_DIVIDEND_METHOD("The configured dividend method is not executable in this run"),
    IBKR_STATEMENT_UNAVAILABLE("IBKR could not provide a statement for the requested date range");

    private final String debugReason;

    IssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    /** Returns the fixed explanation used when inspecting this issue code. */
    public String getDebugReason() {
        return debugReason;
    }
}
