package org.example.autotest;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    private static final Map<String, Object> config;

    static {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("application.yml")) {
            config = yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    public static String getEndPoint() {
        return (String) ((Map<String, Object>) config.get("azure")).get("endpoint");
    }

    public static String getApiKey() {
        return (String) ((Map<String, Object>) config.get("azure")).get("api-key");
    }

    public static String getDeploymentName() {
        return (String) ((Map<String, Object>) config.get("azure")).get("deployment-name");
    }

    public static String getApiVersion() {
        return (String) ((Map<String, Object>) config.get("azure")).get("api-version");
    }

    public static String getUrlFormat() {
        return (String) ((Map<String, Object>) config.get("azure")).get("url-format");
    }

    public static int getMaxToken() {
        return (Integer) ((Map<String, Object>) config.get("azure")).get("max_tokens");
    }

    public static int getConnectTimeout() {
        return (Integer) ((Map<String, Object>) config.get("timeout")).get("connect-timeout");
    }

    public static int getReadTimeout() {
        return (Integer) ((Map<String, Object>) config.get("timeout")).get("read-timeout");
    }

    public static int getWriteTimeout() {
        return (Integer) ((Map<String, Object>) config.get("timeout")).get("write-timeout");
    }

}
