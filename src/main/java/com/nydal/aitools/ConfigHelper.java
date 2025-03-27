package com.nydal.aitools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigHelper {

    private static final Properties properties = new Properties();

    static {
        try {
            // Load properties from the application.properties file
            InputStream inputStream = ConfigHelper.class.getClassLoader().getResourceAsStream("application.properties");
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new RuntimeException("application.properties not found in classpath");
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading application.properties", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getSpeechApiKey() {
        return getProperty("azure.speech.api.key");
    }

    public static String getSpeechEndpoint() {
        return getProperty("azure.speech.endpoint");
    }
}
