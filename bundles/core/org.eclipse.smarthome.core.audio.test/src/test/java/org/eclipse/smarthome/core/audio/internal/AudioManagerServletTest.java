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
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.ByteArrayAudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.internal.fake.AudioSinkFake;
import org.eclipse.smarthome.test.TestPortUtil;
import org.eclipse.smarthome.test.TestServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * OSGi test for {@link AudioManagerImpl}
 *
 * @author Petar Valchev - Initial contribution and API
 * @author Christoph Weitkamp - Added parameter to adjust the volume
 * @author Wouter Born - Migrate tests from Groovy to Java
 * @author Henning Treu - extract servlet tests
 */
public class AudioManagerServletTest {

    private AudioManagerImpl audioManager;

    private AudioSinkFake audioSink;

    private AudioServlet audioServlet;

    private int port;
    private TestServer server;

    private final String AUDIO_SERVLET_PROTOCOL = "http";
    private final String AUDIO_SERVLET_HOSTNAME = "localhost";

    private CompletableFuture<Boolean> serverStarted;

    @Before
    public void setup() {
        audioManager = new AudioManagerImpl();
        audioSink = new AudioSinkFake();

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
    public void audioManagerProcessesMultitimeStreams() throws AudioException {
        audioManager.addAudioSink(audioSink);
        int streamTimeout = 10;
        assertServedStream(streamTimeout);
    }

    @Test
    public void audioManagerProcessesOneTimeStream() throws AudioException {
        audioManager.addAudioSink(audioSink);
        assertServedStream(null);
    }

    @Test
    public void audioManagerDoesNotProcessStreamsIfThereIsNoRegisteredSink() throws AudioException {
        int streamTimeout = 10;
        assertServedStream(streamTimeout);
    }

    private void assertServedStream(Integer timeInterval) throws AudioException {
        AudioStream audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED);
        String url = serveStream(audioStream, timeInterval);

        audioManager.stream(url, audioSink.getId());

        if (audioManager.getSink() == audioSink) {
            assertThat("The streamed url was not as expected", ((URLAudioStream) audioSink.audioStream).getURL(),
                    is(url));
        } else {
            assertThat(String.format("The sink %s received an unexpected stream", audioSink.getId()),
                    audioSink.audioStream, is(nullValue()));
        }
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
        byte[] testByteArray = new byte[] { 0, 1, 2 };

        AudioFormat audioFormat = new AudioFormat(container, codec, true, bitDepth, bitRate, frequency);

        return new ByteArrayAudioStream(testByteArray, audioFormat);
    }

    private String generateURL(String protocol, String hostname, int port, String path) {
        return String.format("%s://%s:%s%s", protocol, hostname, port, path);
    }
}
