package com.helper.issue;

/** Stable machine-readable issue codes. */
public enum IssueCode {
    MISSING_SECURITY_CONFIGURATION("No security configuration exists for this ISIN"),
    UNSUPPORTED_DIVIDEND_METHOD("The configured dividend method is unsupported"),
    UNIMPLEMENTED_DIVIDEND_METHOD("The configured dividend method is not executable in this run"),
    IBKR_STATEMENT_UNAVAILABLE("IBKR could not provide a statement for the requested date range"),
    UNSUPPORTED_ERI_REPORT_FORMAT("No ERI extractor supports the configured report format"),
    ERI_REPORT_LOAD_FAILED("The ERI report could not be loaded or parsed"),
    MISSING_DIVIDEND_OPENING_HOLDING("No opening holding snapshot exists at the dividend boundary"),
    ZERO_DIVIDEND_OPENING_HOLDING("No open position was reported at the dividend boundary"),
    DIVIDEND_FX_RATE_UNAVAILABLE("Required FX rates are unavailable for the dividend report currency"),
    DIVIDEND_PREPARATION_FAILED("The dividend source could not be prepared from the available facts");

    private final String debugReason;

    IssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    /** Returns the fixed explanation used when inspecting this issue code. */
    public String getDebugReason() {
        return debugReason;
    }
}
