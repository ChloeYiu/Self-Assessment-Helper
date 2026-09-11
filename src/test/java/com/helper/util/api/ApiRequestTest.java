package com.helper.util.api;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ApiRequestTest {

    @Test
    public void createGetUri_encodesQueryParameters() {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        queryParameters.put("token", "abc 123");
        queryParameters.put("query", "a+b&c");

        URI uri = ApiRequest.createGetUri("https://example.com", "/api", queryParameters);

        assertEquals("https://example.com/api?token=abc+123&query=a%2Bb%26c", uri.toString());
    }

    @Test
    public void formatDate_usesProvidedFormatter() {
        String value = ApiRequest.formatDate(
                LocalDate.of(2025, 4, 6),
                DateTimeFormatter.BASIC_ISO_DATE);

        assertEquals("20250406", value);
    }

    @Test
    public void sendGet_returnsResponseBodyAndSetsDefaultUserAgent() throws Exception {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(200, "response body");

        String responseBody = ApiRequest.sendGet(
                httpClient,
                URI.create("https://example.com/api"),
                "request failed");

        assertEquals("response body", responseBody);
        assertEquals("Java/Self-Assessment-Helper", httpClient.getRequests()
                .get(0)
                .headers()
                .firstValue("User-Agent")
                .orElseThrow());
    }

    @Test
    public void sendGet_throwsWhenResponseStatusIsNotSuccessful() {
        MockHttpClient httpClient = new MockHttpClient()
                .respondWith(500, "failure");

        IOException exception = assertThrows(
                IOException.class,
                () -> ApiRequest.sendGet(
                        httpClient,
                        URI.create("https://example.com/api"),
                        "request failed"));

        assertEquals("request failed: HTTP 500", exception.getMessage());
    }
}
