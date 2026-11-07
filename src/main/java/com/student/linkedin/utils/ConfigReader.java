package com.student.linkedin.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigReader {

    private static final Properties props = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            props.load(fis);
        } catch (IOException e) {
            throw new RuntimeException(
                "config.properties not found. Copy config.properties.example and fill in your test account details.", e);
        }
    }

    public static String get(String key) {
        // Environment variables override the file (e.g. LINKEDIN_USERNAME)
        String envKey = key.toUpperCase().replace('.', '_');
        String env = System.getenv(envKey);
        return env != null ? env : props.getProperty(key);
    }
}