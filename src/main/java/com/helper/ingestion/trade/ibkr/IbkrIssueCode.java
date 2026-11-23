package com.helper.ingestion.trade.ibkr;

import com.helper.issue.IssueCode;

/** Issues produced while retrieving IBKR Flex statements. */
public enum IbkrIssueCode implements IssueCode {
    STATEMENT_UNAVAILABLE("IBKR could not provide a statement for the requested date range");

    private final String debugReason;

    IbkrIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    @Override
    public String getDebugReason() {
        return debugReason;
    }
}
