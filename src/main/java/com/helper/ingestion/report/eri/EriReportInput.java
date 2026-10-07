package com.helper.ingestion.report.eri;

import com.helper.util.api.ApiRequest;
import com.helper.util.pdf.PdfTextDocument;
import com.helper.util.pdf.PdfTextExtractor;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.util.Objects;

/** Input and report source needed to extract one fund's ERI data. */
public class EriReportInput {
    private final String isin;
    private final int reportingYear;
    private final Path reportPath;
    private final URI reportUri;
    private final HttpClient httpClient;

    /** Creates ERI report input for a local PDF. */
    public EriReportInput(String isin, int reportingYear, Path reportPath) {
        this.isin = Objects.requireNonNull(isin, "isin");
        this.reportingYear = reportingYear;
        this.reportPath = Objects.requireNonNull(reportPath, "reportPath");
        this.reportUri = null;
        this.httpClient = null;
    }

    /** Creates ERI report input for a remote PDF. */
    public EriReportInput(String isin, int reportingYear, URI reportUri) {
        this(isin, reportingYear, reportUri, HttpClient.newHttpClient());
    }

    /** Creates ERI report input for a remote PDF with an HTTP client. */
    public EriReportInput(String isin, int reportingYear, URI reportUri, HttpClient httpClient) {
        this.isin = Objects.requireNonNull(isin, "isin");
        this.reportingYear = reportingYear;
        this.reportPath = null;
        this.reportUri = Objects.requireNonNull(reportUri, "reportUri");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /** Returns the ISIN to find in the ERI report. */
    public String getIsin() {
        return isin;
    }

    /** Returns the reporting year expected in the ERI report. */
    public int getReportingYear() {
        return reportingYear;
    }

    /** Loads the ERI report PDF into a text document. */
    public PdfTextDocument loadPdfTextDocument() throws IOException {
        if (reportPath != null) {
            return PdfTextExtractor.extract(reportPath);
        }
        return PdfTextExtractor.extract(fetchReportBytes(), reportUri.toString());
    }

    private byte[] fetchReportBytes() throws IOException {
        try {
            return ApiRequest.sendGetBytes(httpClient, reportUri, "failed to fetch ERI report");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while fetching ERI report", exception);
        }
    }
}
