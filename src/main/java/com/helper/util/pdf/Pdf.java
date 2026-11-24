package com.helper.util.pdf;

import com.helper.util.api.ApiRequest;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/** Immutable PDF content and its source filename. */
public final class Pdf {
    private final byte[] bytes;
    private final String fileName;

    /** Creates PDF content with a filename retained for provenance and errors. */
    public Pdf(byte[] bytes, String fileName) {
        this.bytes = Objects.requireNonNull(bytes, "bytes").clone();
        this.fileName = Objects.requireNonNull(fileName, "fileName");
        if (bytes.length == 0) {
            throw new IllegalArgumentException("bytes must not be empty");
        }
        if (fileName.isBlank()) {
            throw new IllegalArgumentException("fileName must not be blank");
        }
    }

    /** Reads PDF content from a local path and retains only its bytes and filename. */
    public Pdf(Path path) throws IOException {
        this(Files.readAllBytes(Objects.requireNonNull(path, "path")), fileName(path));
    }

    private static String fileName(Path path) {
        Path fileName = Objects.requireNonNull(path, "path").getFileName();
        if (fileName == null) {
            throw new IllegalArgumentException("path must name a PDF file");
        }
        return fileName.toString();
    }

    /** Downloads PDF content from a URI using the default HTTP client. */
    public Pdf(URI reportUri) throws IOException {
        this(reportUri, HttpClient.newHttpClient());
    }

    /** Downloads PDF content from a URI using supplied HTTP transport. */
    public Pdf(URI reportUri, HttpClient httpClient) throws IOException {
        this(downloadBytes(reportUri, httpClient), fileName(reportUri));
    }

    private static byte[] downloadBytes(URI reportUri, HttpClient httpClient) throws IOException {
        try {
            return ApiRequest.sendGetBytes(
                    Objects.requireNonNull(httpClient, "httpClient"),
                    Objects.requireNonNull(reportUri, "reportUri"),
                    "failed to fetch PDF");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while fetching PDF", exception);
        }
    }

    private static String fileName(URI reportUri) {
        return fileName(Path.of(Objects.requireNonNull(reportUri, "reportUri").getPath()));
    }

    /** Returns a defensive copy of the PDF content. */
    public byte[] getBytes() {
        return bytes.clone();
    }

    /** Returns the source filename for provenance and errors. */
    public String getFileName() {
        return fileName;
    }
}
