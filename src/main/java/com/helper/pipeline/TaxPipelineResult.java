package com.helper.pipeline;

import com.helper.issue.Issue;
import java.util.List;

/**
 * Prepared sources and non-fatal issues from one top-level tax pipeline.
 *
 * <p>{@link IncomeAssembler} selects the registered handler for the concrete result type and
 * adds its sources to the matching income aggregates.</p>
 */
public interface TaxPipelineResult {
    List<Issue> issues();
}
