package com.helper.issue;

/** Stable, component-owned machine-readable issue code. */
public interface IssueCode {
    /** Returns the fixed explanation used when inspecting this issue code. */
    String getDebugReason();
}
