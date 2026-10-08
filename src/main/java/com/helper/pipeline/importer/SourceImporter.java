package com.helper.pipeline.importer;

import java.io.IOException;

/**
 * Imports normalized facts from one provider without making tax-category decisions.
 *
 * @param <I> provider-specific import request type
 */
public interface SourceImporter<I> {
    /**
     * Imports facts for the requested provider and date range.
     *
     * @param input provider-specific credentials, configuration, and requested range
     * @return imported facts and the requested source coverage
     * @throws IOException when the provider source cannot be read
     * @throws InterruptedException when provider retrieval is interrupted
     */
    SourceImportResult importSource(I input) throws IOException, InterruptedException;
}
