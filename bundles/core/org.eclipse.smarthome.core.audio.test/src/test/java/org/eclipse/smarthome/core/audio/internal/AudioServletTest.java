/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.audio.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.ByteArrayAudioStream;
import org.eclipse.smarthome.core.audio.FileAudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.test.TestPortUtil;
import org.eclipse.smarthome.test.TestServer;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test cases for {@link AudioServlet}
 *
 * @author Petar Valchev - Initial contribution
 * @author Wouter Born - Migrate tests from Groovy to Java
 */
public class AudioServletTest extends JavaTest {

    private AudioServlet audioServlet;

    private int port;
    private TestServer server;

    private final static String AUDIO_SERVLET_PROTOCOL = "http";
    private final static String AUDIO_SERVLET_HOSTNAME = "localhost";

    private final String MEDIA_TYPE_AUDIO_WAV = "audio/wav";
    private final String MEDIA_TYPE_AUDIO_OGG = "audio/ogg";
    private final String MEDIA_TYPE_AUDIO_MPEG = "audio/mpeg";

    private static final String CONFIGURATION_DIRECTORY_NAME = "configuration";

    protected static final String MP3_FILE_NAME = "mp3AudioFile.mp3";
    protected static final String MP3_FILE_PATH = CONFIGURATION_DIRECTORY_NAME + "/sounds/" + MP3_FILE_NAME;

    protected static final String WAV_FILE_NAME = "wavAudioFile.wav";
    protected static final String WAV_FILE_PATH = CONFIGURATION_DIRECTORY_NAME + "/sounds/" + WAV_FILE_NAME;

    private CompletableFuture<Boolean> serverStarted;

    private byte[] testByteArray;

    @Before
    public void setup() {
        testByteArray = new byte[] { 0, 1, 2 };

        audioServlet = new AudioServlet();

        ServletHolder servletHolder = new ServletHolder(audioServlet);

        port = TestPortUtil.findFreePort();
        server = new TestServer(AUDIO_SERVLET_HOSTNAME, port, 10000, servletHolder);
        serverStarted = server.startServer();
    }

    @After
    public void tearDown() {
        server.stopServer();
    }

    @Test
    public void audioServletProcessesByteArrayStream() throws Exception {
        AudioStream audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(),
                is(equalTo(MEDIA_TYPE_AUDIO_MPEG)));
    }

    @Test
    public void audioServletProcessesStreamFromWavFile() throws Exception {
        AudioStream audioStream = new FileAudioStream(new File(WAV_FILE_PATH));

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_WAV));
    }

    @Test
    public void audioServletProcessesStreamFromOggContainer() throws Exception {
        AudioStream audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_PCM_SIGNED);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_OGG));
    }

    @Test
    public void mimeTypeIsNullWhenNoContainerAndTheAudioFormatIsNotMp3() throws Exception {
        AudioStream audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response media type was not as expected", response.getMediaType(), is(nullValue()));
    }

    @Test
    public void onlyOneRequestToOneTimeStreamsCanBeMade() throws Exception {
        AudioStream audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3);

        String url = serveStream(audioStream);

        Request request = getHttpRequest(url);

        ContentResponse response = request.send();

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_MPEG));

        response = request.send();

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    @Test
    public void requestToMultitimeStreamCannotBeDoneAfterTheTimeoutOfTheStreamHasExipred() throws Exception {
        AudioStream audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3);

        int streamTimeout = 1;
        String url = serveStream(audioStream, streamTimeout);

        Request request = getHttpRequest(url);

        ContentResponse response = request.send();

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_MPEG));

        assertThat("The audio stream was not added to the multitime streams",
                audioServlet.getMultiTimeStreams().containsValue(audioStream), is(true));

        waitForAssert(() -> {
            try {
                request.send();
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            assertThat("The audio stream was not removed from multitime streams",
                    audioServlet.getMultiTimeStreams().containsValue(audioStream), is(false));
        });

        response = request.send();
        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    private ContentResponse getHttpResponse(AudioStream audioStream) throws Exception {
        String url = serveStream(audioStream);
        return getHttpRequest(url).send();
    }

    private String serveStream(AudioStream stream) {
        return serveStream(stream, null);
    }

    private void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                fail("An exception " + e + " was thrown, while starting the HTTP client");
            }
        }
    }

    private Request getHttpRequest(String url) {
        HttpClient httpClient = new HttpClient();
        startHttpClient(httpClient);
        return httpClient.newRequest(url).method(HttpMethod.GET);
    }

    private String serveStream(AudioStream stream, Integer timeInterval) {
        try {
            serverStarted.get();
        } catch (InterruptedException | ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String path;
        if (timeInterval != null) {
            path = audioServlet.serve((FixedLengthAudioStream) stream, timeInterval);
        } else {
            path = audioServlet.serve(stream);
        }

        return generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, port, path);
    }

    private ByteArrayAudioStream getByteArrayAudioStream(String container, String codec) {
        int bitDepth = 16;
        int bitRate = 1000;
        long frequency = 16384;

        AudioFormat audioFormat = new AudioFormat(container, codec, true, bitDepth, bitRate, frequency);

        return new ByteArrayAudioStream(testByteArray, audioFormat);
    }

    private String generateURL(String protocol, String hostname, int port, String path) {
        return String.format("%s://%s:%s%s", protocol, hostname, port, path);
    }
}
