package com.helper.ingestion.trade.ibkr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;

import com.helper.util.api.MockHttpClient;
import java.net.URI;
import java.time.LocalDate;
import java.util.Properties;
import org.junit.Test;

public class IbkrApiProviderTest {

    @Test
    public void getFlexStatementXml_usesReferenceCodeToFetchStatementXml() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200,
                        "<FlexStatementResponse><Status>Success</Status><ReferenceCode>abc-123</ReferenceCode>"
                                + "</FlexStatementResponse>")
                .respondWith(200, "<FlexQueryResponse><FlexStatements count=\"1\" /></FlexQueryResponse>");

        IbkrApiProvider provider = new IbkrApiProvider(httpClient, createProperties());

        String statementXml = provider.getFlexStatementXml(
                LocalDate.of(2025, 4, 6),
                LocalDate.of(2026, 4, 5));

        assertEquals("<FlexQueryResponse><FlexStatements count=\"1\" /></FlexQueryResponse>", statementXml);

        URI referenceCodeUri = httpClient.getRequestedUris().get(0);
        URI statementUri = httpClient.getRequestedUris().get(1);
        assertTrue(referenceCodeUri.toString().contains("/SendRequest"));
        assertTrue(referenceCodeUri.toString().contains("q=query-id"));
        assertTrue(referenceCodeUri.toString().contains("fd=20250406"));
        assertTrue(referenceCodeUri.toString().contains("td=20260405"));
        assertTrue(statementUri.toString().contains("/GetStatement"));
        assertTrue(statementUri.toString().contains("q=abc-123"));
    }

    @Test
    public void getFlexStatementXml_throwsWhenIbkrReturnsFailureStatus() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200,
                        "<FlexStatementResponse><Status>Fail</Status><ErrorCode>101</ErrorCode>"
                                + "<ErrorMessage>Bad request</ErrorMessage></FlexStatementResponse>");

        IbkrApiProvider provider = new IbkrApiProvider(httpClient, createProperties());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> provider.getFlexStatementXml(LocalDate.of(2025, 4, 6), LocalDate.of(2025, 4, 7)));

        assertEquals("IBKR Flex SendRequest failed: error code 101 - Bad request", exception.getMessage());
    }

    private static Properties createProperties() {
        Properties properties = new Properties();
        properties.setProperty("ibkr.flex.token", "test-token");
        properties.setProperty("ibkr.flex.query-id", "query-id");
        properties.setProperty("ibkr.flex.version", "3");
        return properties;
    }
}
