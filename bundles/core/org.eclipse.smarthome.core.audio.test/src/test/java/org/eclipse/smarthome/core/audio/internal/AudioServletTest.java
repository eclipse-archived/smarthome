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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.junit.Test;

/**
 * OSGi test for {@link AudioServlet}
 *
 * @author Petar Valchev - Initial contribution
 * @author Wouter Born - Migrate tests from Groovy to Java
 */
public class AudioServletTest extends AudioOSGiTest {
    private final String MEDIA_TYPE_AUDIO_WAV = "audio/wav";
    private final String MEDIA_TYPE_AUDIO_OGG = "audio/ogg";
    private final String MEDIA_TYPE_AUDIO_MPEG = "audio/mpeg";

    @Test
    public void audioServletProcessesByteArrayStream() {
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(),
                is(equalTo(MEDIA_TYPE_AUDIO_MPEG)));
    }

    @Test
    public void audioServletProcessesStreamFromWavFile() throws AudioException {
        audioStream = getFileAudioStream(WAV_FILE_PATH);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_WAV));
    }

    @Test
    public void audioServletProcessesStreamFromOggContainer() {
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_PCM_SIGNED);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_OGG));
    }

    @Test
    public void mimeTypeIsNullWhenNoContainerAndTheAudioFormatIsNotMp3() {
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED);

        ContentResponse response = getHttpResponse(audioStream);

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response media type was not as expected", response.getMediaType(), is(nullValue()));
    }

    @Test
    public void onlyOneRequestToOneTimeStreamsCanBeMade()
            throws InterruptedException, TimeoutException, ExecutionException {
        initializeAudioServlet();

        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3);

        String path = audioServlet.serve(audioStream);
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path);

        Request request = getHttpRequest(url);

        ContentResponse response = request.send();

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.OK_200));
        assertThat("The response content was not as expected", response.getContent(), is(testByteArray));
        assertThat("The response media type was not as expected", response.getMediaType(), is(MEDIA_TYPE_AUDIO_MPEG));

        response = request.send();

        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    @Test
    public void requestToMultitimeStreamCannotBeDoneAfterTheTimeoutOfTheStreamHasExipred()
            throws InterruptedException, TimeoutException, ExecutionException {
        initializeAudioServlet();

        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3);

        int streamTimeout = 1;
        String path = audioServlet.serve((FixedLengthAudioStream) audioStream, streamTimeout);
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path);

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
                assertThat("The audio stream was not removed from multitime streams",
                        audioServlet.getMultiTimeStreams().containsValue(audioStream), is(false));
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                throw new IllegalStateException(e);
            }
        });

        response = request.send();
        assertThat("The response status was not as expected", response.getStatus(), is(HttpStatus.NOT_FOUND_404));
    }

    private ContentResponse getHttpResponse(AudioStream audioStream) {
        initializeAudioServlet();
        String path = audioServlet.serve(audioStream);
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path);
        try {
            return getHttpRequest(url).send();
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            throw new IllegalStateException("Failed to HTTP response for audio stream ", e);
        }
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
}
