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

import java.util.Locale;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.io.console.Console;
import org.eclipse.smarthome.io.console.extensions.ConsoleCommandExtension;
import org.junit.Before;
import org.junit.Test;

/**
 * OSGi test for {@link AudioConsoleCommandExtension}
 *
 * @author Petar Valchev - Initial contribution and API
 * @author Christoph Weitkamp - Added parameter to adjust the volume
 * @author Wouter Born - Migrate tests from Groovy to Java
 */
public class AudioConsoleTest extends AudioOSGiTest {
    private AudioConsoleCommandExtension audioConsoleCommandExtension;

    private String consoleOutput;
    private Console consoleMock = new Console() {

        @Override
        public void println(String s) {
            consoleOutput = s;
        }

        @Override
        public void printUsage(String s) {
        }

        @Override
        public void print(String s) {
            consoleOutput = s;
        }
    };

    private int testTimeout = 1;

    @Override
    @Before
    public void setUp() {
        registerSink();
        audioConsoleCommandExtension = getAudioConsoleCommandExtension();
    }

    @Test
    public void audioConsolePlaysFile() throws AudioException {
        audioStream = getFileAudioStream(WAV_FILE_PATH);

        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_PLAY, WAV_FILE_NAME };
        audioConsoleCommandExtension.execute(args, consoleMock);

        assertCompatibleFormat();
    }

    @Test
    public void audioConsolePlaysFileForASpecifiedSink() throws AudioException {
        audioStream = getFileAudioStream(WAV_FILE_PATH);

        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_PLAY, audioSinkFake.getId(), WAV_FILE_NAME };
        audioConsoleCommandExtension.execute(args, consoleMock);

        assertCompatibleFormat();
    }

    @Test
    public void audioConsolePlaysFileForASpecifiedSinkWithASpecifiedVolume() throws AudioException {
        audioStream = getFileAudioStream(WAV_FILE_PATH);

        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_PLAY, audioSinkFake.getId(), WAV_FILE_NAME,
                "25" };
        audioConsoleCommandExtension.execute(args, consoleMock);

        assertCompatibleFormat();
    }

    @Test
    public void audioConsolePlaysFileForASpecifiedSinkWithAnInvalidVolume() {
        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_PLAY, audioSinkFake.getId(), WAV_FILE_NAME,
                "invalid" };
        audioConsoleCommandExtension.execute(args, consoleMock);

        waitForAssert(() -> assertThat("The given volume was invalid", consoleOutput, nullValue()));
    }

    @Test
    public void audioConsolePlaysStream() {
        initializeAudioServlet();

        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED);

        String path = audioServlet.serve((FixedLengthAudioStream) audioStream, testTimeout);
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path);

        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_STREAM, url };
        audioConsoleCommandExtension.execute(args, consoleMock);

        assertThat("The streamed URL was not as expected", ((URLAudioStream) audioSinkFake.audioStream).getURL(),
                is(url));
    }

    @Test
    public void audioConsolePlaysStreamForASpecifiedSink() {
        initializeAudioServlet();

        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED);

        String path = audioServlet.serve((FixedLengthAudioStream) audioStream, testTimeout);
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path);

        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_STREAM, audioSinkFake.getId(), url };
        audioConsoleCommandExtension.execute(args, consoleMock);

        assertThat("The streamed URL was not as expected", ((URLAudioStream) audioSinkFake.audioStream).getURL(),
                is(url));
    }

    @Test
    public void audioConsoleListsSinks() {
        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_SINKS };
        audioConsoleCommandExtension.execute(args, consoleMock);

        waitForAssert(() -> assertThat("The listed sink was not as expected", consoleOutput,
                is(String.format("* %s (%s)", audioSinkFake.getLabel(Locale.getDefault()), audioSinkFake.getId()))));
    }

    @Test
    public void audioConsoleListsSources() {
        registerSource();

        String[] args = new String[] { AudioConsoleCommandExtension.SUBCMD_SOURCES };
        audioConsoleCommandExtension.execute(args, consoleMock);

        waitForAssert(() -> assertThat("The listed source was not as expected", consoleOutput, is(
                String.format("* %s (%s)", audioSourceMock.getLabel(Locale.getDefault()), audioSourceMock.getId()))));
    }

    protected AudioConsoleCommandExtension getAudioConsoleCommandExtension() {
        audioConsoleCommandExtension = getService(ConsoleCommandExtension.class, AudioConsoleCommandExtension.class);

        assertThat("Could not get AudioConsoleCommandExtension", audioConsoleCommandExtension, is(notNullValue()));
        assertThat("Could not get AudioConsoleCommandExtension's usages", audioConsoleCommandExtension.getUsages(),
                is(notNullValue()));

        return audioConsoleCommandExtension;
    }

}
