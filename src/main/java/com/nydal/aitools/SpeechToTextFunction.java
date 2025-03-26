package com.nydal.aitools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.*;
import java.util.Optional;

public class SpeechToTextFunction {
    private static final String SPEECH_API_KEY = System.getenv("AZURE_SPEECH_KEY");
    private static final String SPEECH_REGION = System.getenv("AZURE_SPEECH_REGION");

    @FunctionName("speechToText")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request, final ExecutionContext context) {

        context.getLogger().info("Speech-to-Text function triggered.");

        try {
            String audioBase64 = request.getBody().get();
            String transcript = processSpeech(audioBase64);
            return request.createResponseBuilder(HttpStatus.OK).body(transcript).build();
        } catch (Exception e) {
            context.getLogger().severe("Error processing speech: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR).body("Error").build();
        }
    }

    private String processSpeech(String audioBase64) throws Exception {
        URL url = new URL("https://" + SPEECH_REGION + ".stt.speech.microsoft.com/speech-to-text/v3.1/transcriptions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Ocp-Apim-Subscription-Key", SPEECH_API_KEY);
        conn.setRequestProperty("Content-Type", "audio/wav");
        conn.setDoOutput(true);

        byte[] audioBytes = java.util.Base64.getDecoder().decode(audioBase64);
        OutputStream os = conn.getOutputStream();
        os.write(audioBytes);
        os.close();

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        JsonNode responseJson = new ObjectMapper().readTree(br);
        return responseJson.path("DisplayText").asText();
    }
}
