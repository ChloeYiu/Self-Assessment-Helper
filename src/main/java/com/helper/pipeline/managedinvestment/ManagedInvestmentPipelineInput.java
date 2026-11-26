package com.helper.pipeline.managedinvestment;

import com.helper.util.pdf.Pdf;
import java.util.List;
import java.util.Objects;

/**
 * Selected managed-investment source PDFs for one calculation run.
 *
 * <p>A CLI can create this input from local paths. A web UI can create it from uploaded bytes.
 * Both paths preserve the source filename for provider evidence and user-facing issues.</p>
 */
public final class ManagedInvestmentPipelineInput {
    private final List<Pdf> pdfs;

    /** Creates a run input from selected PDF source documents. */
    public ManagedInvestmentPipelineInput(List<Pdf> pdfs) {
        this.pdfs = List.copyOf(Objects.requireNonNull(pdfs, "pdfs"));
        if (pdfs.isEmpty()) {
            throw new IllegalArgumentException("pdfs must not be empty");
        }
    }

    /** Returns the selected source documents. */
    public List<Pdf> getPdfs() {
        return pdfs;
    }
}
