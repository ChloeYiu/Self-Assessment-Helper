package com.helper.pipeline.managedinvestment.jpmpi;

import com.helper.issue.IssueCode;

/** Issues produced while importing a JPMPI annual tax pack. */
public enum JpmpiIssueCode implements IssueCode {
    TAX_PACK_TOTALS_UNAVAILABLE("JPMPI tax-pack totals could not be extracted");

    private final String debugReason;

    JpmpiIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    @Override
    public String getDebugReason() {
        return debugReason;
    }
}
