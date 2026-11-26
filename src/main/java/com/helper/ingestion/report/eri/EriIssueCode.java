package com.helper.ingestion.report.eri;

import com.helper.issue.IssueCode;

/** Issues produced while selecting or loading an ERI report. */
public enum EriIssueCode implements IssueCode {
    UNSUPPORTED_REPORT_FORMAT("No ERI extractor supports the configured report format"),
    REPORT_LOAD_FAILED("The ERI report could not be loaded or parsed");

    private final String debugReason;

    EriIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    @Override
    public String getDebugReason() {
        return debugReason;
    }
}
