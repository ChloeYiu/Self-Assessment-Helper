package com.helper.util.pdf;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Selects relevant text lines from an extracted PDF text document.
 */
public class PdfTextFilter {
    /** Creates a PDF text filter. */
    public PdfTextFilter() {
    }

    /**
     * Finds lines accepted by the supplied line predicate.
     *
     * @param document extracted PDF text document
     * @param predicate line predicate
     * @return matching lines in document order
     */
    public List<PdfTextLine> matching(PdfTextDocument document, Predicate<PdfTextLine> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return Objects.requireNonNull(document, "document")
                .getLines()
                .stream()
                .filter(predicate)
                .toList();
    }

    /**
     * Finds lines whose text is accepted by the supplied text predicate.
     *
     * @param document extracted PDF text document
     * @param predicate text predicate
     * @return matching lines in document order
     */
    public List<PdfTextLine> matchingText(PdfTextDocument document, Predicate<String> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return matching(document, line -> predicate.test(line.getText()));
    }

    /**
     * Finds the first line accepted by the supplied line predicate.
     *
     * @param document extracted PDF text document
     * @param predicate line predicate
     * @return first matching line, if present
     */
    public Optional<PdfTextLine> firstMatching(PdfTextDocument document, Predicate<PdfTextLine> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return Objects.requireNonNull(document, "document")
                .getLines()
                .stream()
                .filter(predicate)
                .findFirst();
    }

    /**
     * Finds the first line whose text is accepted by the supplied text predicate.
     *
     * @param document extracted PDF text document
     * @param predicate text predicate
     * @return first matching line, if present
     */
    public Optional<PdfTextLine> firstMatchingText(PdfTextDocument document, Predicate<String> predicate) {
        Objects.requireNonNull(predicate, "predicate");
        return firstMatching(document, line -> predicate.test(line.getText()));
    }

    /**
     * Selects a line range between the first start marker and following end marker.
     *
     * @param document extracted PDF text document
     * @param startPredicate text predicate identifying the range start
     * @param endPredicate text predicate identifying the range end
     * @param includeStart whether to include the matched start line
     * @param includeEnd whether to include the matched end line
     * @return selected lines in document order, or an empty list when no valid range is found
     */
    public List<PdfTextLine> between(
            PdfTextDocument document,
            Predicate<String> startPredicate,
            Predicate<String> endPredicate,
            boolean includeStart,
            boolean includeEnd) {
        List<PdfTextLine> lines = Objects.requireNonNull(document, "document").getLines();
        Objects.requireNonNull(startPredicate, "startPredicate");
        Objects.requireNonNull(endPredicate, "endPredicate");

        int startIndex = findTextIndex(lines, startPredicate, 0);
        if (startIndex < 0) {
            return List.of();
        }

        int endIndex = findTextIndex(lines, endPredicate, startIndex + 1);
        if (endIndex < 0) {
            return List.of();
        }

        int fromIndex = includeStart ? startIndex : startIndex + 1;
        int toIndex = includeEnd ? endIndex + 1 : endIndex;
        if (fromIndex > toIndex) {
            return List.of();
        }
        return lines.subList(fromIndex, toIndex);
    }

    /**
     * Selects lines around a known extracted line.
     *
     * @param document extracted PDF text document
     * @param line center line
     * @param before number of lines before the center line
     * @param after number of lines after the center line
     * @return selected lines in document order
     */
    public List<PdfTextLine> around(PdfTextDocument document, PdfTextLine line, int before, int after) {
        if (before < 0) {
            throw new IllegalArgumentException("before must not be negative");
        }
        if (after < 0) {
            throw new IllegalArgumentException("after must not be negative");
        }

        List<PdfTextLine> lines = Objects.requireNonNull(document, "document").getLines();
        int centerIndex = lines.indexOf(Objects.requireNonNull(line, "line"));
        if (centerIndex < 0) {
            return List.of();
        }

        int startIndex = Math.max(0, centerIndex - before);
        int endIndex = Math.min(lines.size(), centerIndex + after + 1);
        return lines.subList(startIndex, endIndex);
    }

    private static int findTextIndex(List<PdfTextLine> lines, Predicate<String> predicate, int fromIndex) {
        for (int index = fromIndex; index < lines.size(); index++) {
            if (predicate.test(lines.get(index).getText())) {
                return index;
            }
        }
        return -1;
    }
}
