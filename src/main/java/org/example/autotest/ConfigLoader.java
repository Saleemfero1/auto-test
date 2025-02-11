package org.example.autotest;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigLoader {
    private static final Map<String, Object> config;
    public static final String AZURE = "azure";
    public static final String TIMEOUT = "timeout";

    static {
        loadSecrets();
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("application.yml")) {
            config = yaml.load(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration", e);
        }
    }

    private static void loadSecrets() {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream("secrets.yml")) {
            Map<String, Object> secrets = yaml.load(inputStream);
            Map<String, String> azure = (Map<String, String>) secrets.get(AZURE);
            System.setProperty("AZURE_API_KEY", azure.get("api-key"));
            System.setProperty("AZURE_ENDPOINT", azure.get("endpoint"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load secrets", e);
        }
    }

    public static String getEndPoint() {
        return System.getProperty("AZURE_ENDPOINT");
    }

    public static String getApiKey() {
        return System.getProperty("AZURE_API_KEY");
    }

    public static String getDeploymentName() {
        return (String) ((Map<String, Object>) config.get(AZURE)).get("deployment-name");
    }

    public static String getApiVersion() {
        return (String) ((Map<String, Object>) config.get(AZURE)).get("api-version");
    }

    public static String getUrlFormat() {
        return (String) ((Map<String, Object>) config.get(AZURE)).get("url-format");
    }

    public static int getMaxToken() {
        return (Integer) ((Map<String, Object>) config.get(AZURE)).get("max_tokens");
    }

    public static int getConnectTimeout() {
        return (Integer) ((Map<String, Object>) config.get(TIMEOUT)).get("connect-timeout");
    }

    public static int getReadTimeout() {
        return (Integer) ((Map<String, Object>) config.get(TIMEOUT)).get("read-timeout");
    }

    public static int getWriteTimeout() {
        return (Integer) ((Map<String, Object>) config.get(TIMEOUT)).get("write-timeout");
    }

}
