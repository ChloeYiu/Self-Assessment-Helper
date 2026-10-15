package com.helper.issue;

import java.util.Map;
import java.util.Objects;

/** Structured non-fatal condition retained by an application stage. */
public class Issue {
    private final IssueCode code;
    private final IssueSeverity severity;
    private final String source;
    private final Map<String, String> attributes;

    /** Creates an issue. */
    public Issue(
            IssueCode code,
            IssueSeverity severity,
            String source,
            Map<String, String> attributes
    ) {
        this.code = Objects.requireNonNull(code, "code");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.source = requireNonBlank(source, "source");
        this.attributes = Map.copyOf(Objects.requireNonNull(attributes, "attributes"));
    }

    /** Returns the stable machine-readable issue code. */
    public IssueCode getCode() {
        return code;
    }

    /** Returns the issue severity. */
    public IssueSeverity getSeverity() {
        return severity;
    }

    /** Returns the source that produced this issue. */
    public String getSource() {
        return source;
    }

    /** Returns the fixed explanation for this issue code when debugging. */
    public String getDebugReason() {
        return code.getDebugReason();
    }

    /** Returns structured issue attributes for UI/report rendering. */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    private static String requireNonBlank(String value, String name) {
        String result = Objects.requireNonNull(value, name).trim();
        if (result.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return result;
    }
}
