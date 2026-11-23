package com.helper.pipeline.selfdirectedinvestment.consolidation;

import com.helper.issue.IssueCode;

/** Issues produced while consolidating imported source facts. */
public enum SourceConsolidationIssueCode implements IssueCode {
    ZERO_DIVIDEND_OPENING_HOLDING("No open position was reported at the dividend boundary");

    private final String debugReason;

    SourceConsolidationIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    @Override
    public String getDebugReason() {
        return debugReason;
    }
}
