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

import static org.eclipse.smarthome.core.audio.internal.AudioManagerImpl.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.function.BiFunction;

import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.audio.internal.fake.AudioSinkFake;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.junit.Test;

/**
 * OSGi test for {@link AudioManagerImpl}
 *
 * @author Petar Valchev - Initial contribution and API
 * @author Christoph Weitkamp - Added parameter to adjust the volume
 * @author Wouter Born - Migrate tests from Groovy to Java
 */
public class AudioManagerTest extends AudioOSGiTest {

    @Test
    public void audioManagerPlaysByteArrayAudioStream() throws AudioException {
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_MP3);
        assertProcessedStreamTypeStream(audioStream);
    }

    @Test
    public void nullStreamsAreProcessed() {
        registerSink();

        audioManager.play(null, audioSinkFake.getId());

        waitForAssert(
                () -> assertThat("The 'null' stream was not processed", audioSinkFake.isStreamProcessed, is(true)));
        assertThat("The currently playing stream was not stopped", audioSinkFake.isStreamStopped, is(true));
    }

    @Test
    public void audioManagerPlaysStreamFromWavAudioFiles() throws AudioException {
        audioStream = getFileAudioStream(WAV_FILE_PATH);
        assertProcessedStreamTypeStream(audioStream);
    }

    @Test
    public void audioManagerPlaysStreamFromMp3AudioFiles() throws AudioException {
        audioStream = getFileAudioStream(MP3_FILE_PATH);
        assertProcessedStreamTypeStream(audioStream);
    }

    @Test
    public void audioManagerPlaysWavAudioFiles() throws AudioException {
        audioStream = getFileAudioStream(WAV_FILE_PATH);
        assertProcessedStreamTypeFile(WAV_FILE_NAME);
    }

    @Test
    public void audioManagerPlaysMp3AudioFiles() throws AudioException {
        audioStream = getFileAudioStream(MP3_FILE_PATH);
        assertProcessedStreamTypeFile(MP3_FILE_NAME);
    }

    @Test
    public void fileIsNotProcessedIfThereIsNoRegisteredSink() throws AudioException {
        File file = new File(MP3_FILE_PATH);
        assertThat(String.format("The file %s does not exist", file.getName()), file.exists(), is(true));

        audioSinkFake = new AudioSinkFake();

        audioManager.playFile(file.getName(), audioSinkFake.getId());

        waitForAssert(() -> assertThat(String.format("The file %s was processed", file.getName()),
                audioSinkFake.isStreamProcessed, is(false)));
    }

    @Test
    public void audioManagerHandlesUnsupportedAudioFormatException() throws AudioException {
        registerSink();

        audioStream = getFileAudioStream(MP3_FILE_PATH);

        audioSinkFake.isUnsupportedAudioFormatExceptionExpected = true;
        try {
            audioManager.playFile(MP3_FILE_NAME, audioSinkFake.getId());
        } catch (UnsupportedAudioFormatException e) {
            fail("An exception " + e + " was thrown, while trying to process a stream");
        }
    }

    @Test
    public void audioManagerHandlesUnsupportedAudioStreamException() throws AudioException {
        registerSink();

        audioStream = getFileAudioStream(MP3_FILE_PATH);

        audioSinkFake.isUnsupportedAudioStreamExceptionExpected = true;
        try {
            audioManager.playFile(MP3_FILE_NAME, audioSinkFake.getId());
        } catch (UnsupportedAudioStreamException e) {
            fail("An exception " + e + " was thrown, while trying to process a stream");
        }
    }

    @Test
    public void audioManagerSetsTheVolumeOfASink() throws IOException {
        registerSink();

        PercentType initialVolume = new PercentType(67);
        PercentType sinkMockVolume = getSinkMockVolume(initialVolume);

        waitForAssert(
                () -> assertThat(String.format("The volume of the sink %s was not as expected", audioSinkFake.getId()),
                        sinkMockVolume, is(initialVolume)));
    }

    @Test
    public void theVolumeOfANullSinkIsZero() throws IOException {
        assertThat("The volume was not as expected", audioManager.getVolume(null), is(PercentType.ZERO));
    }

    @Test
    public void audioManagerSetsTheVolumeOfNotRegisteredSinkToZero() throws IOException {
        audioSinkFake = new AudioSinkFake();

        PercentType initialVolume = new PercentType(67);
        PercentType sinkMockVolume = getSinkMockVolume(initialVolume);

        waitForAssert(
                () -> assertThat(String.format("The volume of the sink %s was not as expected", audioSinkFake.getId()),
                        sinkMockVolume, is(PercentType.ZERO)));
    }

    @Test
    public void sourceIsRegistered() {
        assertRegisteredSource(false);
    }

    @Test
    public void defaultSourceIsRegistered() {
        assertRegisteredSource(true);
    }

    @Test
    public void sinkIsRegistered() {
        assertRegisteredSink(false);
    }

    @Test
    public void defaultSinkIsRegistered() {
        assertRegisteredSink(true);
    }

    @Test
    public void sinkIsAddedInParameterOptions() {
        assertAddedParameterOption(AudioManagerImpl.CONFIG_DEFAULT_SINK, Locale.getDefault());
    }

    @Test
    public void sourceIsAddedInParameterOptions() {
        assertAddedParameterOption(AudioManagerImpl.CONFIG_DEFAULT_SOURCE, Locale.getDefault());
    }

    @Test
    public void inCaseOfWrongUriNoParameterOptionsAreAdded() {
        registerSink();

        Collection<ParameterOption> parameterOptions = audioManager.getParameterOptions(URI.create("wrong.uri"),
                AudioManagerImpl.CONFIG_DEFAULT_SINK, Locale.US);
        assertThat("The parameter options were not as expected", parameterOptions, is(nullValue()));
    }

    @Test
    public void audioManagerProcessesMultitimeStreams() throws AudioException {
        registerSink();
        int streamTimeout = 10;
        assertServedStream(streamTimeout);
    }

    @Test
    public void audioManagerProcessesOneTimeStream() throws AudioException {
        registerSink();
        assertServedStream(null);
    }

    @Test
    public void audioManagerDoesNotProcessStreamsIfThereIsNoRegisteredSink() throws AudioException {
        audioSinkFake = new AudioSinkFake();
        int streamTimeout = 10;
        assertServedStream(streamTimeout);
    }

    private void assertRegisteredSource(boolean isSourceDefault) {
        registerSource();

        if (isSourceDefault) {
            audioManager.modified(Collections.singletonMap(CONFIG_DEFAULT_SOURCE, audioSourceMock.getId()));
        } else {
            // just to make sure there is no default source
            audioManager.modified(Collections.emptyMap());
        }

        assertThat(String.format("The source %s was not registered", audioSourceMock.getId()), audioManager.getSource(),
                is(audioSourceMock));
        assertThat(String.format("The source %s was not added to the set of sources", audioSourceMock.getId()),
                audioManager.getAllSources().contains(audioSourceMock), is(true));
        assertThat(String.format("The source %s was not added to the set of sources", audioSourceMock.getId()),
                audioManager.getSourceIds(audioSourceMock.getId()).contains(audioSourceMock.getId()), is(true));
    }

    private void assertRegisteredSink(boolean isSinkDefault) {
        registerSink();

        if (isSinkDefault) {
            audioManager.modified(Collections.singletonMap(CONFIG_DEFAULT_SINK, audioSinkFake.getId()));
        } else {
            // just to make sure there is no default sink
            audioManager.modified(Collections.emptyMap());
        }

        assertThat(String.format("The sink %s was not registered", audioSinkFake.getId()), audioManager.getSink(),
                is(audioSinkFake));
        assertThat(String.format("The sink %s was not added to the set of sinks", audioSinkFake.getId()),
                audioManager.getAllSinks().contains(audioSinkFake), is(true));
        assertThat(String.format("The sink %s was not added to the set of sinks", audioSinkFake.getId()),
                audioManager.getSinkIds(audioSinkFake.getId()).contains(audioSinkFake.getId()), is(true));
    }

    private void assertProcessedStreamTypeStream(AudioStream audioStream) throws AudioException {
        registerSink();

        waitForAssert(() -> assertThat("The format of the audio sink mock was not as expected",
                audioSinkFake.audioFormat, is(nullValue())));

        assertThat("The currently playing stream was stopped", audioSinkFake.isStreamStopped, is(false));
        audioManager.play(audioStream, audioSinkFake.getId());

        assertCompatibleFormat();
    }

    private void assertProcessedStreamTypeFile(String audioFileName) throws AudioException {
        registerSink();

        waitForAssert(() -> assertThat("The format of the audio sink mock was not as expected",
                audioSinkFake.audioFormat, is(nullValue())));

        assertThat("The currently playing stream was stopped", audioSinkFake.isStreamStopped, is(false));

        audioManager.playFile(audioFileName, audioSinkFake.getId());

        assertCompatibleFormat();
    }

    private PercentType getSinkMockVolume(PercentType initialVolume) throws IOException {
        audioManager.setVolume(initialVolume, audioSinkFake.getId());

        String sinkMockId = audioSinkFake.getId();
        return audioManager.getVolume(sinkMockId);
    }

    /**
     *
     * @param param - either default source or default sink
     */
    private void assertAddedParameterOption(String param, Locale locale) {
        String id = "";
        String label = "";

        switch (param) {
            case AudioManagerImpl.CONFIG_DEFAULT_SINK:
                registerSink();
                id = audioSinkFake.getId();
                label = audioSinkFake.getLabel(locale);
                break;
            case AudioManagerImpl.CONFIG_DEFAULT_SOURCE:
                registerSource();
                id = audioSourceMock.getId();
                label = audioSourceMock.getLabel(locale);
                break;
            default:
                fail("The parameter must be either default sink or default source");
        }

        Collection<ParameterOption> parameterOptions = audioManager
                .getParameterOptions(URI.create(AudioManagerImpl.CONFIG_URI), param, locale);

        BiFunction<String, String, Boolean> isParameterOptionAdded = (v, l) -> parameterOptions.stream()
                .anyMatch(po -> po.getValue().equals(v) && po.getLabel().equals(l));

        assertThat(param + " was not added to the parameter options", isParameterOptionAdded.apply(id, label),
                is(equalTo(true)));
    }

    private void assertServedStream(Integer timeInterval) throws AudioException {
        initializeAudioServlet();

        String path;
        audioStream = getByteArrayAudioStream(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED);
        if (timeInterval != null) {
            path = audioServlet.serve((FixedLengthAudioStream) audioStream, timeInterval);
        } else {
            path = audioServlet.serve(audioStream);
        }
        String url = generateURL(AUDIO_SERVLET_PROTOCOL, AUDIO_SERVLET_HOSTNAME, AUDIO_SERVLET_PORT, path);

        audioManager.stream(url, audioSinkFake.getId());

        if (audioManager.getSink() == audioSinkFake) {
            assertThat("The streamed url was not as expected", ((URLAudioStream) audioSinkFake.audioStream).getURL(),
                    is(url));
        } else {
            assertThat(String.format("The sink %s received an unexpected stream", audioSinkFake.getId()),
                    audioSinkFake.audioStream, is(nullValue()));
        }
    }

    private enum PlayType {
        STREAM("stream"),
        FILE("file");

        private String playType;

        PlayType(String playType) {
            this.playType = playType;
        }

        public String getPlayType() {
            return playType;
        }
    }
}
