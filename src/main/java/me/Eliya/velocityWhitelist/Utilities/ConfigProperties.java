package me.Eliya.velocityWhitelist.Utilities;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.*;
import java.util.Map;

public class ConfigProperties {
    private final Path configPath;
    private Map<String, Object> config;

    public ConfigProperties(Path configPath) {
        this.configPath = configPath;
    }

    public void loadConfig() {
        try {
            if (!Files.exists(configPath)) {
                Files.createDirectories(configPath.getParent());

                InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
                if (defaultConfig == null) {
                    throw new IllegalStateException("Default configuration file 'config.yml' is missing. Ensure it is included in the JAR.");
                }

                Files.copy(defaultConfig, configPath);
            }

            try (InputStream inputStream = Files.newInputStream(configPath)) {
                Yaml yaml = createYaml();
                config = yaml.load(inputStream);
            }
        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }
    }

    private Yaml createYaml() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return new Yaml(options);
    }

    public Object get(String key) {
        return config.get(key);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public void saveConfig() {
        try (OutputStream outputStream = Files.newOutputStream(configPath);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {

            Yaml yaml = createYaml();
            yaml.dump(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        loadConfig();
    }

    public Map<String, Object> getConfig() {
        return config;
    }
}
