package com.helper.ingestion.report.eri;

import static org.junit.Assert.assertEquals;

import com.helper.config.CurrencyCode;
import com.helper.income.implementation.dividend.model.AccumulatingFundReport;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class SsgaSpdrEriReportExtractorTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void extractReport_readsSsgaSpdrReportLineForRequestedIsin() throws IOException {
        Path pdfPath = temporaryFolder.newFile("ssga-spdr-report.pdf").toPath();
        writePdf(
                pdfPath,
                "Reporting Period Ended 31 March 2025",
                "SPDR MSCI ACWI UCITS ETF IE00B44Z5B48 ACWI USD 1.2345 distribution 30 September 2025"
        );

        AccumulatingFundReport report = new SsgaSpdrEriReportExtractor().extractReport(new EriReportInput(
                "IE00B44Z5B48",
                2025,
                pdfPath,
                null
        ));

        assertEquals("IE00B44Z5B48", report.getFundIdentifier());
        assertEquals(LocalDate.of(2024, 4, 1), report.getReportingPeriodStartDate());
        assertEquals(LocalDate.of(2025, 3, 31), report.getReportingPeriodEndDate());
        assertEquals(LocalDate.of(2025, 9, 30), report.getFundDistributionDate());
        assertBigDecimalEquals("1.2345", report.getReportedIncomePerUnit());
        assertEquals(CurrencyCode.USD, report.getCurrencyCode());
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractReport_throwsWhenDistributionDateIsNotSixMonthsAfterReportingPeriodEnd() throws IOException {
        Path pdfPath = temporaryFolder.newFile("ssga-spdr-report-invalid-date.pdf").toPath();
        writePdf(
                pdfPath,
                "Reporting Period Ended 31 March 2025",
                "SPDR MSCI ACWI UCITS ETF IE00B44Z5B48 ACWI USD 1.2345 distribution 29 September 2025"
        );

        new SsgaSpdrEriReportExtractor().extractReport(new EriReportInput(
                "IE00B44Z5B48",
                2025,
                pdfPath,
                null
        ));
    }

    private static void writePdf(Path path, String... lines) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                content.newLineAtOffset(72, 720);
                for (String line : lines) {
                    content.showText(line);
                    content.newLineAtOffset(0, -16);
                }
                content.endText();
            }
            document.save(path.toFile());
        }
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
