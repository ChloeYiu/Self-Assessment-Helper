package com.helper.ingestion.report.eri;

import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import com.helper.util.api.ApiRequest;
import com.helper.util.pdf.Pdf;
import com.helper.util.pdf.PdfTextDocument;
import com.helper.util.pdf.PdfTextExtractor;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.util.Objects;

/**
 * Loads an issuer ERI report and extracts one fund's reportable-income data from it.
 */
public abstract class EriReportExtractor {
    private final HttpClient httpClient;

    /** Creates an extractor that uses the default HTTP client. */
    protected EriReportExtractor() {
        this(HttpClient.newHttpClient());
    }

    /** Creates an extractor with an HTTP client supplied for transport or tests. */
    protected EriReportExtractor(HttpClient httpClient) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient");
    }

    /**
     * Loads the reporting-period PDF and extracts the named fund's report data.
     *
     * @param isin fund share-class identifier to find in the report
     * @param reportingYear calendar year in which the reporting period ends
     * @return the extracted reportable-income data for the requested fund
     * @throws IOException when the report cannot be retrieved or read
     */
    public final AccumulatingFundReport extractReport(String isin, int reportingYear) throws IOException {
        requireNonBlank(isin, "isin");
        URI reportUri = resolveReportUri(reportingYear);
        PdfTextDocument document = loadReportDocument(reportUri);
        return parseReport(document, isin, reportingYear);
    }

    /** Resolves the issuer-specific report URI for a reporting year. */
    protected abstract URI resolveReportUri(int reportingYear);

    /** Parses one fund's row from the already downloaded issuer report. */
    protected abstract AccumulatingFundReport parseReport(
            PdfTextDocument document,
            String isin,
            int reportingYear
    );

    private PdfTextDocument loadReportDocument(URI reportUri) throws IOException {
        try {
            byte[] reportBytes = ApiRequest.sendGetBytes(
                    httpClient,
                    Objects.requireNonNull(reportUri, "reportUri"),
                    "failed to fetch ERI report");
            return PdfTextExtractor.extract(new Pdf(reportBytes, reportUri.toString()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while fetching ERI report", exception);
        }
    }

    private static String requireNonBlank(String value, String name) {
        String result = Objects.requireNonNull(value, name);
        if (result.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return result;
    }
}
