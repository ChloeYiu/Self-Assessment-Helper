package com.helper.income.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Registry of configured instrument tax metadata, indexed by ISIN. */
public class SecurityConfigs {
    private static final Path DEFAULT_CONFIG_PATH = Path.of("config/security-configs.yaml");

    private final Map<String, SecurityConfig> configsByIsin;

    /** Creates a registry from configured instruments. */
    public SecurityConfigs(List<SecurityConfig> configs) {
        this.configsByIsin = indexByIsin(configs);
    }

    /** Loads the default security config registry. */
    public static SecurityConfigs load() throws IOException {
        return load(DEFAULT_CONFIG_PATH);
    }

    /** Loads a security config registry from a YAML file. */
    public static SecurityConfigs load(Path configPath) throws IOException {
        List<String> lines = Files.readAllLines(Objects.requireNonNull(configPath, "configPath"));
        return new SecurityConfigs(parse(lines));
    }

    /** Returns the configured instrument for an ISIN when present. */
    public Optional<SecurityConfig> findByIsin(String isin) {
        return Optional.ofNullable(configsByIsin.get(Objects.requireNonNull(isin, "isin")));
    }

    /** Returns whether the registry contains an instrument for an ISIN. */
    public boolean containsIsin(String isin) {
        return configsByIsin.containsKey(Objects.requireNonNull(isin, "isin"));
    }

    /** Returns all configured instruments. */
    public List<SecurityConfig> getAll() {
        return List.copyOf(configsByIsin.values());
    }

    private static Map<String, SecurityConfig> indexByIsin(List<SecurityConfig> configs) {
        Map<String, SecurityConfig> result = new LinkedHashMap<>();
        for (SecurityConfig config : Objects.requireNonNull(configs, "configs")) {
            SecurityConfig value = Objects.requireNonNull(config, "config");
            if (result.putIfAbsent(value.getIsin(), value) != null) {
                throw new IllegalArgumentException("duplicate instrument ISIN " + value.getIsin());
            }
        }
        return result;
    }

    private static List<SecurityConfig> parse(List<String> lines) {
        List<SecurityConfig> configs = new ArrayList<>();
        InstrumentBuilder builder = null;
        Section section = Section.NONE;

        for (String rawLine : lines) {
            String line = stripComment(rawLine);
            if (line.isBlank() || "instruments:".equals(line.trim())) {
                continue;
            }

            String trimmed = line.trim();
            if (trimmed.startsWith("- ")) {
                if (builder != null) {
                    configs.add(builder.build());
                }
                builder = new InstrumentBuilder();
                section = Section.NONE;
                String content = trimmed.substring(2).trim();
                if (!content.isBlank()) {
                    readInstrumentValue(builder, section, content);
                }
                continue;
            }

            if (builder == null) {
                // If we haven't seen a '-' line yet, then this is an invalid config file.
                throw new IllegalArgumentException("instrument entry must start with '-'");
            }

            if ("dividendCalculationMethod:".equals(trimmed)) {
                section = Section.DIVIDEND;
                continue;
            }
            if ("capitalGainCalculationMethod:".equals(trimmed)) {
                section = Section.CAPITAL_GAIN;
                continue;
            }

            readInstrumentValue(builder, section, trimmed);
        }

        if (builder != null) {
            configs.add(builder.build());
        }
        return configs;
    }

    private static void readInstrumentValue(InstrumentBuilder builder, Section section, String content) {
        KeyValue keyValue = parseKeyValue(content);
        if (section == Section.DIVIDEND) {
            builder.readDividend(keyValue);
        } else if (section == Section.CAPITAL_GAIN) {
            builder.readCapitalGain(keyValue);
        } else {
            builder.readRoot(keyValue);
        }
    }

    private static KeyValue parseKeyValue(String content) {
        int separator = content.indexOf(':');
        if (separator < 0) {
            throw new IllegalArgumentException("invalid instrument config line: " + content);
        }
        return new KeyValue(content.substring(0, separator).trim(), content.substring(separator + 1).trim());
    }

    private static String stripComment(String line) {
        int commentIndex = line.indexOf('#');
        if (commentIndex < 0) {
            return line;
        }
        return line.substring(0, commentIndex);
    }

    private enum Section {
        NONE,
        DIVIDEND,
        CAPITAL_GAIN
    }

    private record KeyValue(String key, String value) {
    }

    private static class InstrumentBuilder {
        private String isin;
        private SecurityConfigEnums.DividendCalculationMethodType dividendType;
        private SecurityConfigEnums.EriReportFormat eriReportFormat;
        private SecurityConfigEnums.CapitalGainCalculationMethodType capitalGainType;

        private void readRoot(KeyValue keyValue) {
            if ("isin".equals(keyValue.key())) {
                isin = keyValue.value();
            }
        }

        private void readDividend(KeyValue keyValue) {
            if ("type".equals(keyValue.key())) {
                dividendType = SecurityConfigEnums.DividendCalculationMethodType.valueOf(keyValue.value());
            } else if ("eriReportFormat".equals(keyValue.key())) {
                eriReportFormat = SecurityConfigEnums.EriReportFormat.valueOf(keyValue.value());
            }
        }

        private void readCapitalGain(KeyValue keyValue) {
            if ("type".equals(keyValue.key())) {
                capitalGainType = SecurityConfigEnums.CapitalGainCalculationMethodType.valueOf(keyValue.value());
            }
        }

        private SecurityConfig build() {
            DividendCalculationMethod dividendMethod = dividendType == null
                    ? null
                    : new DividendCalculationMethod(dividendType, eriReportFormat);
            CapitalGainCalculationMethod capitalGainMethod = capitalGainType == null
                    ? null
                    : new CapitalGainCalculationMethod(capitalGainType);
            return new SecurityConfig(isin, dividendMethod, capitalGainMethod);
        }
    }
}
