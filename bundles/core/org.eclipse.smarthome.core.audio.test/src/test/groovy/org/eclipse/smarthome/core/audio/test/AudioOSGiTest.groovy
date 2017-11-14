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

import org.eclipse.smarthome.config.core.ConfigConstants
import org.eclipse.smarthome.core.audio.*
import org.eclipse.smarthome.core.audio.internal.AudioManagerImpl
import org.eclipse.smarthome.core.audio.internal.AudioServlet
import org.eclipse.smarthome.core.audio.test.fake.AudioSinkFake
import org.eclipse.smarthome.test.OSGiTest
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass

/**
 * The base class for the OSGi tests for org.eclipse.smarthome.core.audio.
 *
 * This class is extended by the classes that test {@link AudioManagerImpl}, {@link AudioServlet}, {@link AudioConsoleCommandExtension} and {@link AudioFormat}.
 *
 * @author Petar Valchev
 *
 */
class AudioOSGiTest extends OSGiTest {
    protected AudioManager audioManager
    protected AudioSink audioSinkFake
    protected AudioStream audioStream
    protected AudioServlet audioServlet

    protected def audioSourceMock

    protected final String AUDIO_SERVLET_PROTOCOL = "http"
    protected final String AUDIO_SERVLET_HOSTNAME = "localhost"
    protected final int AUDIO_SERVLET_PORT = 9090

    protected byte[] testByteArray = [0, 1, 2]

    private static String defaultConfigDir

    private static final String CONFIGURATION_DIRECTORY_NAME = "configuration"

    protected def final WAV_FILE_PATH = "$CONFIGURATION_DIRECTORY_NAME/sounds/wavAudioFile.wav"
    protected def final MP3_FILE_PATH = "$CONFIGURATION_DIRECTORY_NAME/sounds/mp3AudioFile.mp3"

    @BeforeClass
    public static void setUpClass(){
        // Store the initial value for the configuration directory property, so that it can be restored at the test end.
        defaultConfigDir = System.getProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT)
        // Set new configuration directory for test purposes.
        System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, CONFIGURATION_DIRECTORY_NAME)
    }

    @Before
    public void setUp(){
        audioManager = getService(AudioManager, AudioManagerImpl)
    }

    @After
    public void tearDown(){
        if(audioStream != null){
            audioStream.close()
        }
    }

    @AfterClass
    public static void tearDownClass(){
        if(defaultConfigDir != null){
            // Set the value for the configuration directory property to its initial one.
            System.setProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT, defaultConfigDir)
        } else {
            System.clearProperty(ConfigConstants.CONFIG_DIR_PROG_ARGUMENT)
        }
    }

    protected void registerSource(){
        audioSourceMock = [getId: { -> 'testSourceId'}, getLabel: { Locale locale -> 'testSourceLabel'}] as AudioSource
        registerService(audioSourceMock, AudioSource.class.getName())
    }

    protected void registerSink(){
        audioSinkFake = new AudioSinkFake()
        registerService(audioSinkFake, AudioSink.class.getName())
    }

    protected ByteArrayAudioStream getByteArrayAudioStream(String container, String codec){
        int bitDepth = 16
        int bitRate = 1000
        long frequency = 16384

        AudioFormat audioFormat = new AudioFormat(container, codec,
                true, bitDepth, bitRate, frequency)

        AudioStream byteArrayAudioStream = new ByteArrayAudioStream(testByteArray, audioFormat)

        return byteArrayAudioStream
    }

    protected FileAudioStream getFileAudioStream(String path){
        File audioFile = new File(path)
        assertThat "The file ${audioFile.getName()} does not exist",
                audioFile.exists(),
                is(true)

        FileAudioStream fileAudioStream = new FileAudioStream(audioFile)
        return fileAudioStream
    }

    protected void assertCompatibleFormat(){
        AudioFormat expextedAudioFormat = audioStream.getFormat()
        AudioFormat sinkAudioFormat = audioSinkFake.audioFormat

        waitForAssert({
            assertThat "The sink ${audioSinkFake.getId()} was not updated with the expected audioFormat $expextedAudioFormat",
                    sinkAudioFormat.isCompatible(expextedAudioFormat),
                    is(true)
        })
    }

    protected void initializeAudioServlet(){
        waitForAssert {
            audioServlet = getService(AudioHTTPServer, AudioServlet)
            assertThat "Could not find AudioServlet",
                    audioServlet,
                    is(notNullValue())
        }
    }

    protected String generateURL(String protocol,String hostname,int port,String path) {
        String url = "$protocol://$hostname:$port$path";
        return url
    }
}
