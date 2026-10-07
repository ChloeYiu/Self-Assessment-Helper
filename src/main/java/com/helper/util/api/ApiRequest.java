package com.helper.util.api;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Utility methods for common API request behavior.
 */
public final class ApiRequest {
    private static final String DEFAULT_USER_AGENT = "Java/Self-Assessment-Helper";

    private ApiRequest() {
    }

    public static String sendGet(
            HttpClient httpClient,
            URI uri,
            String failureMessage) throws IOException, InterruptedException {
        return sendGet(httpClient, uri, DEFAULT_USER_AGENT, failureMessage);
    }

    public static String sendGet(
            HttpClient httpClient,
            URI uri,
            String userAgent,
            String failureMessage) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(Objects.requireNonNull(uri, "uri"))
                .header("User-Agent", Objects.requireNonNull(userAgent, "userAgent"))
                .GET()
                .build();

        HttpResponse<String> response = Objects.requireNonNull(httpClient, "httpClient")
                .send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(failureMessage + ": HTTP " + response.statusCode());
        }

        return response.body();
    }

    public static byte[] sendGetBytes(
            HttpClient httpClient,
            URI uri,
            String failureMessage) throws IOException, InterruptedException {
        return sendGetBytes(httpClient, uri, DEFAULT_USER_AGENT, failureMessage);
    }

    public static byte[] sendGetBytes(
            HttpClient httpClient,
            URI uri,
            String userAgent,
            String failureMessage) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(Objects.requireNonNull(uri, "uri"))
                .header("User-Agent", Objects.requireNonNull(userAgent, "userAgent"))
                .GET()
                .build();

        HttpResponse<byte[]> response = Objects.requireNonNull(httpClient, "httpClient")
                .send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(failureMessage + ": HTTP " + response.statusCode());
        }

        return response.body();
    }

    public static URI createGetUri(String baseUrl, String path, Map<String, String> queryParameters) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(queryParameters, "queryParameters");

        String queryString = queryParameters.entrySet()
                .stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .collect(Collectors.joining("&"));

        return URI.create(baseUrl + path + "?" + queryString);
    }

    public static String formatDate(LocalDate date, DateTimeFormatter formatter) {
        return Objects.requireNonNull(formatter, "formatter")
                .format(Objects.requireNonNull(date, "date"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(Objects.requireNonNull(value, "value"), StandardCharsets.UTF_8);
    }
}
