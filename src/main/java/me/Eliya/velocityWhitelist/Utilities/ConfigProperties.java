package me.Eliya.velocityWhitelist.Utilities;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ConfigProperties {
    private final Path configPath;
    private Map<String, Object> config;

    public ConfigProperties(Path configPath) {
        this.configPath = configPath;
    }

    public void loadConfig() {
        if (!Files.exists(configPath)) {
            try {
                Files.createDirectories(configPath.getParent());
                InputStream defaultConfig = getClass().getResourceAsStream("/config.yml");
                Files.copy(defaultConfig, configPath);
            }
            catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            Yaml yaml = new Yaml();
            config = yaml.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return (String) config.get(key);
    }

    public void set(String key, Object value) {
        config.put(key, value);
    }

    public void saveConfig() {
        try (OutputStream outputStream = Files.newOutputStream(configPath);
             OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
            Yaml yaml = new Yaml();
            yaml.dump(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
