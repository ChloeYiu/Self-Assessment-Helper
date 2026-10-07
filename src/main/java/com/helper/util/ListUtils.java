package com.helper.util;

import java.util.List;

/** Utility methods for immutable collection handling. */
public final class ListUtils {
    private ListUtils() {
    }

    /** Returns an immutable copy of the list, or an empty list when the input is null. */
    public static <T> List<T> copyOrEmpty(List<T> values) {
        if (values == null) {
            return List.of();
        }
        return List.copyOf(values);
    }
}
