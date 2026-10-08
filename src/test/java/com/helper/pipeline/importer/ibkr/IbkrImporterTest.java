package com.helper.pipeline.importer.ibkr;

import static org.junit.Assert.assertEquals;

import com.helper.ingestion.trade.ibkr.IbkrApiProvider;
import com.helper.ingestion.trade.ibkr.IbkrSecurityExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTestFixtures;
import com.helper.ingestion.trade.ibkr.IbkrTradeExtractor;
import com.helper.ingestion.trade.ibkr.IbkrTradeMapper;
import com.helper.pipeline.importer.SourceImportResult;
import com.helper.util.api.MockHttpClient;
import com.helper.util.table.TabularDocument;
import java.time.LocalDate;
import java.util.Properties;
import org.junit.Test;

public class IbkrImporterTest {
    @Test
    public void importSource_returnsDetectedSecurityAndTradeForRequestedRange() throws Exception {
        LocalDate startDate = LocalDate.of(2025, 4, 6);
        LocalDate endDate = LocalDate.of(2026, 4, 5);
        IbkrApiProvider provider = new IbkrApiProvider(
                new MockHttpClient()
                        .respondWith(200, "<FlexStatementResponse><Status>Success</Status>"
                                + "<ReferenceCode>abc-123</ReferenceCode></FlexStatementResponse>")
                        .respondWith(200, "<FlexQueryResponse />"),
                properties());

        IbkrImporter importer = new IbkrImporter(
                provider,
                new StubIbkrTradeMapper(),
                new IbkrSecurityExtractor(),
                new IbkrTradeExtractor());

        SourceImportResult result = importer.importSource(new IbkrImportInput(null, startDate, endDate));

        assertEquals(startDate, result.getRequestedStartDate());
        assertEquals(endDate, result.getRequestedEndDate());
        assertEquals(1, result.getSecurities().size());
        assertEquals("IE00B44Z5B48", result.getSecurities().get(0).getIdentifier());
        assertEquals(1, result.getTrades().size());
        assertEquals("BUY_1", result.getTrades().get(0).getTradeId());
    }

    private static Properties properties() {
        Properties properties = new Properties();
        properties.setProperty("ibkr.flex.token", "test-token");
        properties.setProperty("ibkr.flex.query-id", "query-id");
        properties.setProperty("ibkr.flex.version", "3");
        return properties;
    }

    private static class StubIbkrTradeMapper extends IbkrTradeMapper {
        @Override
        public TabularDocument extractFlexStatementDocument(String flexXml) {
            return IbkrTestFixtures.acwiBuyDocument();
        }
    }
}
