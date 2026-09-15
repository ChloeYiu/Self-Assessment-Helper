package com.helper.ingestion.report.eri;

import java.net.URI;
import java.nio.file.Path;
import java.util.Objects;

/** Input needed to extract one fund's ERI data from a report source. */
public class EriReportInput {
    private final String isin;
    private final int reportingYear;
    private final Path localReportPath;
    private final URI reportUri;

    /** Creates ERI report input. */
    public EriReportInput(String isin, int reportingYear, Path localReportPath, URI reportUri) {
        this.isin = Objects.requireNonNull(isin, "isin");
        this.reportingYear = reportingYear;
        this.localReportPath = localReportPath;
        this.reportUri = reportUri;
    }

    /** Returns the ISIN to find in the ERI report. */
    public String getIsin() {
        return isin;
    }

    /** Returns the reporting year expected in the ERI report. */
    public int getReportingYear() {
        return reportingYear;
    }

    /** Returns the local report PDF path, or null when the report is not local. */
    public Path getLocalReportPath() {
        return localReportPath;
    }

    /** Returns the report URI, or null when the report is not remote. */
    public URI getReportUri() {
        return reportUri;
    }
}
