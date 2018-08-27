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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.ByteArrayAudioStream;
import org.eclipse.smarthome.core.audio.FileAudioStream;
import org.eclipse.smarthome.core.audio.internal.fake.AudioSinkFake;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * The base class for the OSGi tests for org.eclipse.smarthome.core.audio.
 *
 * This class is extended by the classes that test {@link AudioManagerImpl}, {@link AudioServlet},
 * {@link AudioConsoleCommandExtension} and {@link AudioFormat}.
 *
 * @author Petar Valchev - Initial contribution
 * @author Wouter Born - Migrate tests from Groovy to Java
 */
public class AudioOSGiTest extends JavaOSGiTest {
    protected AudioManagerImpl audioManager;
    protected AudioSinkFake audioSinkFake;
    protected AudioStream audioStream;
    protected AudioServlet audioServlet;

    protected AudioSource audioSourceMock;

    protected final String AUDIO_SERVLET_PROTOCOL = "http";
    protected final String AUDIO_SERVLET_HOSTNAME = "localhost";
    protected final int AUDIO_SERVLET_PORT = 9090;

    protected byte[] testByteArray = new byte[] { 0, 1, 2 };

    private static String defaultConfigDir;

    private static final String CONFIGURATION_DIRECTORY_NAME = "configuration";

    protected static final String MP3_FILE_NAME = "mp3AudioFile.mp3";
    protected static final String MP3_FILE_PATH = CONFIGURATION_DIRECTORY_NAME + "/sounds/" + MP3_FILE_NAME;

    protected static final String WAV_FILE_NAME = "wavAudioFile.wav";
    protected static final String WAV_FILE_PATH = CONFIGURATION_DIRECTORY_NAME + "/sounds/" + WAV_FILE_NAME;

    @BeforeClass
    public static void setUpClass() {
        // Store the initial value for the configuration directory property, so that it can be restored at the test end.
        defaultConfigDir = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT);
        // Set new configuration directory for test purposes.
        System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, CONFIGURATION_DIRECTORY_NAME);
    }

    @Before
    public void setUp() {
        audioManager = getService(AudioManager.class, AudioManagerImpl.class);
    }

    @After
    public void tearDown() throws IOException {
        if (audioStream != null) {
            audioStream.close();
        }
    }

    @AfterClass
    public static void tearDownClass() {
        if (defaultConfigDir != null) {
            // Set the value for the configuration directory property to its initial one.
            System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, defaultConfigDir);
        } else {
            System.clearProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT);
        }
    }

    protected void registerSource() {
        audioSourceMock = new AudioSource() {

            @Override
            public Set<AudioFormat> getSupportedFormats() {
                return Collections.emptySet();
            }

            @Override
            public String getLabel(Locale locale) {
                return "testSourceLabel";
            }

            @Override
            public AudioStream getInputStream(AudioFormat format) throws AudioException {
                return null;
            }

            @Override
            public String getId() {
                return "testSourceId";
            }
        };

        registerService(audioSourceMock, AudioSource.class.getName());
    }

    protected void registerSink() {
        audioSinkFake = new AudioSinkFake();
        registerService(audioSinkFake, AudioSink.class.getName());
    }

    protected ByteArrayAudioStream getByteArrayAudioStream(String container, String codec) {
        int bitDepth = 16;
        int bitRate = 1000;
        long frequency = 16384;

        AudioFormat audioFormat = new AudioFormat(container, codec, true, bitDepth, bitRate, frequency);

        return new ByteArrayAudioStream(testByteArray, audioFormat);
    }

    protected FileAudioStream getFileAudioStream(String path) throws AudioException {
        File audioFile = new File(path);
        assertThat(String.format("The file %s does not exist", audioFile.getName()), audioFile.exists(), is(true));
        return new FileAudioStream(audioFile);
    }

    protected void assertCompatibleFormat() {
        AudioFormat expectedAudioFormat = audioStream.getFormat();
        AudioFormat sinkAudioFormat = audioSinkFake.audioFormat;

        waitForAssert(
                () -> assertThat(
                        String.format("The sink %s was not updated with the expected audioFormat %s",
                                audioSinkFake.getId(), expectedAudioFormat),
                        sinkAudioFormat.isCompatible(expectedAudioFormat), is(true)));
    }

    protected void initializeAudioServlet() {
        waitForAssert(() -> {
            audioServlet = getService(AudioHTTPServer.class, AudioServlet.class);
            assertThat("Could not find AudioServlet", audioServlet, is(notNullValue()));
        });
    }

    protected String generateURL(String protocol, String hostname, int port, String path) {
        return String.format("%s://%s:%s%s", protocol, hostname, port, path);
    }
}
