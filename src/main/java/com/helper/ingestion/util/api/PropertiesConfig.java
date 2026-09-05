package com.helper.ingestion.util.api;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;

/**
 * Utility methods for loading required properties from local config files.
 */
public final class PropertiesConfig {
    private PropertiesConfig() {
    }

    public static Properties load(Path configPath) {
        Path path = Objects.requireNonNull(configPath, "configPath");
        Properties properties = new Properties();

        try (InputStream inputStream = Files.newInputStream(path)) {
            properties.load(inputStream);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Unable to load config: " + path, exception);
        }

        return properties;
    }

    public static String requireProperty(Properties properties, String propertyName) {
        String value = Objects.requireNonNull(properties, "properties").getProperty(propertyName);

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required config property: " + propertyName);
        }

        return value.trim();
    }
}
