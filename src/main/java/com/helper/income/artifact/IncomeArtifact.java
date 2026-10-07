package com.helper.income.artifact;

/**
 * Marker contract for side-products created while calculating income.
 */
public interface IncomeArtifact {

    /**
     * Stable artifact type for routing to a handler.
     */
    IncomeArtifactType getArtifactType();

    /**
     * Process this artifact.
     */
    void process();
}
