package com.nydal.aitools;

import com.microsoft.azure.functions.*;
import com.microsoft.cognitiveservices.speech.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SpeechToTextFunctionTest {

    private SpeechToTextFunction function;
    private ExecutionContext context;
    private HttpRequestMessage<Optional<InputStream>> request;
    private Logger mockLogger;

    @BeforeEach
    public void setup() {
        // Initialize the function and mock dependencies
        function = new SpeechToTextFunction();
        context = mock(ExecutionContext.class);
        request = mock(HttpRequestMessage.class);

        // Mock Logger to prevent NullPointerException
        mockLogger = mock(Logger.class);
        when(context.getLogger()).thenReturn(mockLogger);

        // Mock createResponseBuilder to return a mock HttpResponseMessage.Builder
        HttpResponseMessage.Builder responseBuilder = mock(HttpResponseMessage.Builder.class);
        when(request.createResponseBuilder(any(HttpStatus.class))).thenReturn(responseBuilder);

        // Mock the body() method to return the same responseBuilder (so you can chain it)
        when(responseBuilder.body(any())).thenReturn(responseBuilder);
    }

    @Test
    public void testRun_withValidAudio_returnsTranscript() throws Exception {
        // Prepare mock input stream and other necessary mock behavior
        String mockTranscript = "Hello World";
        byte[] audioData = new byte[] {1, 2, 3, 4, 5}; // Some mock audio data
        InputStream mockAudioStream = new ByteArrayInputStream(audioData);
        when(request.getBody()).thenReturn(Optional.of(mockAudioStream));

        // Mock the processAudioStream method to return a mock transcript
        SpeechConfig speechConfig = mock(SpeechConfig.class);

        // Mock the behavior of processAudioStream
        String result = "Hello World";
        SpeechToTextFunction spyFunction = Mockito.spy(function);
        //doReturn(result).when(spyFunction).processAudioStream(any(InputStream.class), any(SpeechConfig.class), any(ExecutionContext.class));

        // Call the function
        HttpResponseMessage response = spyFunction.run(request, context);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockTranscript, response.getBody().toString());
    }

    @Test
    public void testRun_withNoAudio_returnsError() {
        // Simulate no audio in the request
        when(request.getBody()).thenReturn(Optional.empty());

        // Call the function
        HttpResponseMessage response = function.run(request, context);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertTrue(response.getBody().toString().contains("Error processing speech"));
    }

    @Test
    public void testProcessAudioStream_withValidData_returnsTranscript() throws ExecutionException, InterruptedException {
        // Mock speechConfig
        SpeechConfig speechConfig = mock(SpeechConfig.class);

        // Mock ExecutionContext
        ExecutionContext context = mock(ExecutionContext.class);

        // Prepare a mock audio input stream
        byte[] audioData = new byte[] {1, 2, 3, 4, 5};
        InputStream audioStream = new ByteArrayInputStream(audioData);

        // Create a spy of SpeechToTextFunction to mock the behavior of the processAudioStream method
        SpeechToTextFunction spyFunction = Mockito.spy(function);
        String expectedTranscript = "Mocked Transcript";

        // Mock the processing behavior
        //doReturn(expectedTranscript).when(spyFunction).processAudioStream(any(InputStream.class), any(SpeechConfig.class), any(ExecutionContext.class));

        // Call the method directly for testing processAudioStream logic
        String result = spyFunction.processAudioStream(audioStream, speechConfig, context);

        // Assertions
        assertNotNull(result);
        assertEquals(expectedTranscript, result);
    }
}
