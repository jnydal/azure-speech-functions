package com.nydal.aitools;

import com.microsoft.azure.functions.*;
import com.microsoft.cognitiveservices.speech.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
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
    }

    @Test
    public void testRun_withValidAudio_returnsTranscript() throws Exception {
        // Mock HttpResponseMessage and its builder
        HttpResponseMessage.Builder responseBuilder = mock(HttpResponseMessage.Builder.class);
        HttpResponseMessage mockResponse = mock(HttpResponseMessage.class);

        // Prepare mock input stream and other necessary mock behavior
        String mockTranscript = "Hello World";
        byte[] audioData = new byte[] {1, 2, 3, 4, 5}; // Some mock audio data
        InputStream mockAudioStream = new ByteArrayInputStream(audioData);
        when(request.getBody()).thenReturn(Optional.of(mockAudioStream));

        // Mock the processAudioStream method to return a mock transcript
        SpeechConfig speechConfig = mock(SpeechConfig.class);
        String result = "Hello World";
        SpeechToTextFunction spyFunction = Mockito.spy(function);
        doReturn(result).when(spyFunction).processAudioStream(mockAudioStream, speechConfig, context);

        // Ensure createResponseBuilder returns the responseBuilder mock
        when(request.createResponseBuilder(HttpStatus.OK)).thenReturn(responseBuilder);
        when(responseBuilder.body(any())).thenReturn(responseBuilder);
        when(responseBuilder.header(any(), any())).thenReturn(responseBuilder);
        when(responseBuilder.status(HttpStatus.OK)).thenReturn(responseBuilder);
        when(mockResponse.getBody()).thenReturn(mockTranscript);
        when(mockResponse.getStatus()).thenReturn(HttpStatus.OK);
        when(responseBuilder.build()).thenReturn(mockResponse);

        // Call the function
        HttpResponseMessage response = spyFunction.run(request, context);

        // Assertions
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(mockTranscript, response.getBody());
    }

}
