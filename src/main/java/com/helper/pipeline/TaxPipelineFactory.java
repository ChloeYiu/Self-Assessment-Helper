package com.helper.pipeline;

import com.helper.support.registry.Registry;
import java.io.IOException;

/** Creates one registry-bound income-category pipeline for a tax calculation run. */
@FunctionalInterface
public interface TaxPipelineFactory {
    TaxPipeline create(Registry registry) throws IOException;
}
