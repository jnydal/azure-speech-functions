package com.nydal.aitools;

import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.*;

import java.io.InputStream;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class SpeechToTextFunction {

    @FunctionName("realTimeSpeechToText")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<InputStream>> request,
            final ExecutionContext context
    ) {
        try {
            // Get API Key and Endpoint from environment variables
            String apiKey = ConfigHelper.getSpeechApiKey();
            URI endpoint = URI.create(ConfigHelper.getSpeechEndpoint());

            // Initialize Speech API Config
            SpeechConfig speechConfig = SpeechConfig.fromEndpoint(endpoint, apiKey);

            // Get audio stream from HTTP request
            InputStream audioStream = request.getBody().orElseThrow(() -> new IllegalArgumentException("Audio stream not found"));
            // Call method to process streaming
            String transcript = processAudioStream(audioStream, speechConfig, context);

            // Return the transcribed text
            return request.createResponseBuilder(HttpStatus.OK)
                    .body(transcript)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing speech: " + e.getMessage())
                    .build();
        }
    }

    protected String processAudioStream(InputStream audioStream, SpeechConfig speechConfig, final ExecutionContext context) throws ExecutionException, InterruptedException {
        StringBuilder transcript = new StringBuilder();

        // Create PushAudioInputStream for real-time streaming
        PushAudioInputStream pushStream = AudioInputStream.createPushStream();
        AudioConfig audioConfig = AudioConfig.fromStreamInput(pushStream);
        SpeechRecognizer recognizer = new SpeechRecognizer(speechConfig, audioConfig);

        // Event Listener for recognized speech
        recognizer.recognized.addEventListener((s, e) -> {
            if (e.getResult().getReason() == ResultReason.RecognizedSpeech) {
                String text = e.getResult().getText();
                context.getLogger().info("Recognized: " + text);
                transcript.append(text).append(" ");
            }
        });

        // Start recognition
        recognizer.startContinuousRecognitionAsync().get();

        // Feed audio data into the PushAudioInputStream
        byte[] buffer = new byte[4096];
        int bytesRead;
        try {
            while ((bytesRead = audioStream.read(buffer)) != -1) {
                pushStream.write(buffer);
            }
        } catch (Exception e) {
            context.getLogger().severe("Error reading audio stream: " + e.getMessage());
        }

        // Stop recognition
        recognizer.stopContinuousRecognitionAsync().get();
        pushStream.close();

        return transcript.toString();
    }
}
