package com.helper.util.pdf;

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

    /** Returns a defensive copy of the PDF content. */
    public byte[] getBytes() {
        return bytes.clone();
    }

    /** Returns the source filename for provenance and errors. */
    public String getFileName() {
        return fileName;
    }
}
