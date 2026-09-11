package com.helper.util.api;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PropertiesConfigTest {

    @Test
    public void load_readsPropertiesFile() throws IOException {
        Path configPath = Files.createTempFile("api-config", ".properties");
        Files.writeString(configPath, "api.token=abc123\n");

        Properties properties = PropertiesConfig.load(configPath);

        assertEquals("abc123", properties.getProperty("api.token"));
    }

    @Test
    public void requireProperty_returnsTrimmedValue() {
        Properties properties = new Properties();
        properties.setProperty("api.token", "  abc123  ");

        String value = PropertiesConfig.requireProperty(properties, "api.token");

        assertEquals("abc123", value);
    }

    @Test
    public void requireProperty_rejectsMissingProperty() {
        Properties properties = new Properties();

        assertThrows(
                IllegalArgumentException.class,
                () -> PropertiesConfig.requireProperty(properties, "api.token"));
    }

    @Test
    public void requireProperty_rejectsBlankProperty() {
        Properties properties = new Properties();
        properties.setProperty("api.token", "   ");

        assertThrows(
                IllegalArgumentException.class,
                () -> PropertiesConfig.requireProperty(properties, "api.token"));
    }
}
