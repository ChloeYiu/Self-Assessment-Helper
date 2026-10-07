package com.helper.support.registry;

import java.util.Objects;

/**
 * Typed key for values stored in a run registry.
 *
 * @param <T> value type associated with this key
 */
public final class RegistryKey<T> {
    private final String name;
    private final Class<T> type;

    private RegistryKey(String name, Class<T> type) {
        this.name = Objects.requireNonNull(name, "name");
        this.type = Objects.requireNonNull(type, "type");
    }

    /** Creates a typed registry key. */
    public static <T> RegistryKey<T> of(String name, Class<T> type) {
        return new RegistryKey<>(name, type);
    }

    /** Returns the stable key name. */
    public String getName() {
        return name;
    }

    /** Returns the value type expected for this key. */
    public Class<T> getType() {
        return type;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RegistryKey<?> registryKey)) {
            return false;
        }
        return name.equals(registryKey.name) && type.equals(registryKey.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
