package com.helper.ingestion.trade.ibkr;

import com.helper.ingestion.Extractor;
import com.helper.ingestion.util.table.TabularDocument;
import java.io.IOException;
import java.nio.file.Path;

/** Extracts issuer ERI reports for holdings imported from IBKR. */
public class IbkrEriReportExtractor implements Extractor<TabularDocument> {
    /** Creates a deterministic ERI report extractor. */
    public IbkrEriReportExtractor() {
    }

    /**
     * Extracts an ERI report into the common tabular document representation.
     * Deterministic PDF text parsing and ERI field mapping are pending.
     */
    public TabularDocument extractReportDocument(Path report, String isin, int reportingYear)
            throws IOException {
        throw new UnsupportedOperationException();
    }
}
