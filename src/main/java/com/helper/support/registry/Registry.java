package com.helper.support.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Generic registry for source documents, extracted models, cached support data, and artifacts.
 */
public class Registry {
    private final Map<RegistryKey<?>, Object> values;

    /** Creates an empty run registry. */
    public Registry() {
        this.values = new HashMap<>();
    }

    /** Stores a value under a typed registry key. */
    public <T> void put(RegistryKey<T> key, T value) {
        RegistryKey<T> registryKey = Objects.requireNonNull(key, "key");
        T registryValue = Objects.requireNonNull(value, "value");
        if (!registryKey.getType().isInstance(registryValue)) {
            throw new IllegalArgumentException("value type does not match registry key " + registryKey.getName());
        }

        values.put(registryKey, registryValue);
    }

    /** Returns a value for a typed registry key when present. */
    public <T> Optional<T> get(RegistryKey<T> key) {
        RegistryKey<T> registryKey = Objects.requireNonNull(key, "key");
        Object value = values.get(registryKey);
        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(registryKey.getType().cast(value));
    }

    /** Returns a value for a typed registry key, or fails if it is missing. */
    public <T> T require(RegistryKey<T> key) {
        RegistryKey<T> registryKey = Objects.requireNonNull(key, "key");
        return get(registryKey)
                .orElseThrow(() -> new IllegalArgumentException("missing registry value " + registryKey.getName()));
    }

    /** Returns whether a typed registry key has a stored value. */
    public boolean contains(RegistryKey<?> key) {
        return values.containsKey(Objects.requireNonNull(key, "key"));
    }

    /** Removes all stored values from this run registry. */
    public void clear() {
        values.clear();
    }
}
