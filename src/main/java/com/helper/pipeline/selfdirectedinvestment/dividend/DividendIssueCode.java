package com.helper.pipeline.selfdirectedinvestment.dividend;

import com.helper.issue.IssueCode;

/** Issues produced while preparing self-directed dividend inputs. */
public enum DividendIssueCode implements IssueCode {
    UNIMPLEMENTED_METHOD("The configured dividend method is not executable in this run"),
    MISSING_OPENING_HOLDING("No opening holding snapshot exists at the dividend boundary"),
    FX_RATE_UNAVAILABLE("Required FX rates are unavailable for the dividend report currency"),
    PREPARATION_FAILED("The dividend source could not be prepared from the available facts");

    private final String debugReason;

    DividendIssueCode(String debugReason) {
        this.debugReason = debugReason;
    }

    @Override
    public String getDebugReason() {
        return debugReason;
    }
}
