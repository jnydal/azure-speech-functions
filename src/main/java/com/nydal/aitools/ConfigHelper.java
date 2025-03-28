package com.nydal.aitools;

import static java.util.Objects.isNull;

public class ConfigHelper {

    public static String getSpeechApiKey() {
        String key = System.getenv("AZURE_SPEECH_API_KEY");
        if (isNull(key)) {
            return "";
        }
        return key;
    }

    public static String getSpeechEndpoint() {
        String endpoint = System.getenv("AZURE_SPEECH_ENDPOINT");
        if (isNull(endpoint)) {
            return "http://dummy.com";
        }
        return endpoint;
    }
}
