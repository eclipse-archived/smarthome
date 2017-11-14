/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.ContentResponse
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.http.HttpMethod
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.smarthome.core.audio.*
import org.junit.Test

/**
 * OSGi test for {@link AudioServlet}
 *
 * @author Petar Valchev
 *
 */
public class AudioServletTest extends AudioOSGiTest {
    private final String MEDIA_TYPE_AUDIO_WAV = "audio/wav"
    private final String MEDIA_TYPE_AUDIO_OGG = "audio/ogg"
    private final String MEDIA_TYPE_AUDIO_MPEG = "audio/mpeg"

    @Test
    public void 'audio servlet processes byte array stream'(){
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3)

        ContentResponse response = getHttpResponse(audioStream)

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.OK_200)
        assertThat "The response content was not as expected",
                response.getContent(),
                is(testByteArray)
        assertThat "The response media type was not as expected", response.getMediaType(), is(equalTo(MEDIA_TYPE_AUDIO_MPEG))
    }

    @Test
    public void 'audio servlet processes stream from wav file'(){
        audioStream = getFileAudioStream(WAV_FILE_PATH)

        ContentResponse response = getHttpResponse(audioStream)

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.OK_200)
        assertThat "The response media type was not as expected",
                response.getMediaType(),
                is(MEDIA_TYPE_AUDIO_WAV)
    }

    @Test
    public void 'audio servlet processes stream from ogg container'(){
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_OGG, AudioFormat.CODEC_PCM_SIGNED)

        ContentResponse response = getHttpResponse(audioStream)

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.OK_200)
        assertThat "The response content was not as expected",
                response.getContent(),
                is(testByteArray)
        assertThat "The response media type was not as expected",
                response.getMediaType(),
                is(MEDIA_TYPE_AUDIO_OGG)
    }

    @Test
    public void 'mime type is null when no container and the audio format is not mp3'(){
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_PCM_SIGNED)

        ContentResponse response = getHttpResponse(audioStream)

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.OK_200)
        assertThat "The response media type was not as expected",
                response.getMediaType(),
                is(nullValue())
    }

    @Test
    public void 'only one request to one time streams can be made'(){
        initializeAudioServlet()

        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3)

        String path = audioServlet.serve(audioStream)
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path)

        Request request = getHttpRequest(url)

        ContentResponse response = request.send()

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.OK_200)
        assertThat "The response content was not as expected",
                response.getContent(),
                is(testByteArray)
        assertThat "The response media type was not as expected",
                response.getMediaType(),
                is(MEDIA_TYPE_AUDIO_MPEG)

        response = request.send()

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.NOT_FOUND_404)
    }

    @Test
    public void 'request to multitime stream cannot be done after the timeout of the stream has exipred'(){
        initializeAudioServlet()

        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3)

        int streamTimeout = 1
        String path = audioServlet.serve(audioStream, streamTimeout)
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path)

        Request request = getHttpRequest(url)

        ContentResponse response = request.send()

        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.OK_200)
        assertThat "The response content was not as expected",
                response.getContent(),
                is(testByteArray)
        assertThat "The response media type was not as expected",
                response.getMediaType(),
                is(MEDIA_TYPE_AUDIO_MPEG)

        assertThat "The audio stream was not added to the multitime streams",
                audioServlet.multiTimeStreams.containsValue(audioStream),
                is(true)

        waitForAssert({
            response = request.send()
            assertThat "The audio stream was not removed from multitime streams",
                    audioServlet.multiTimeStreams.containsValue(audioStream),
                    is(false)
        })

        response = request.send()
        assertThat "The response status was not as expected",
                response.getStatus(),
                is(HttpStatus.NOT_FOUND_404)
    }

    private ContentResponse getHttpResponse(AudioStream audioStream){
        initializeAudioServlet()

        String path = audioServlet.serve(audioStream)
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path)

        Request request = getHttpRequest(url)

        ContentResponse response = request.send()

        return response
    }

    private void startHttpClient(HttpClient client) {
        if (!client.isStarted()) {
            try {
                client.start();
            } catch (Exception e) {
                fail("An exception $e was thrown, while starting the HTTP client");
            }
        }
    }

    private Request getHttpRequest(String url){
        HttpClient httpClient = new HttpClient()
        startHttpClient(httpClient)
        Request request = httpClient.newRequest(url).method(HttpMethod.GET)

        return request
    }
}
