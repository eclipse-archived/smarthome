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

import org.eclipse.smarthome.core.audio.*
import org.junit.Test

/**
 * OSGi test for {@link AudioFormat}
 *
 * @author Petar Valchev
 *
 */
public class AudioFormatTest extends AudioOSGiTest {
    private String testContainer =  AudioFormat.CONTAINER_WAVE
    private String testCodec = AudioFormat.CODEC_PCM_SIGNED
    private boolean testBigEndian = true
    private Integer testBitDepth = new Integer(16)
    private Integer testBitRate = new Integer(1000)
    private Long testFrequency = new Long(1024)

    @Test
    public void 'there is no best match for an audio format if one of the fileds is null'(){
        Set<AudioFormat> inputs = new HashSet<>()
        Set<AudioFormat> outputs = new HashSet<>()

        AudioFormat nullContainerAudioFormat = new AudioFormat(null, testCodec,
                testBigEndian, testBitDepth, testBitRate, testFrequency)
        AudioFormat nullCodecAudioFormat = new AudioFormat(testContainer, null,
                testBigEndian, testBitDepth, testBitRate, testFrequency)
        AudioFormat nullBitDepthAudioFormat = new AudioFormat(testContainer, testCodec,
                testBigEndian, null, testBitRate, testFrequency)
        AudioFormat nullBitRateAudioFormat = new AudioFormat(testContainer, testCodec,
                testBigEndian, testBitDepth, null, testFrequency)
        AudioFormat nullFrequencyAudioFormat = new AudioFormat(testContainer, testCodec,
                testBigEndian, testBitDepth, testBitRate, null)
        AudioFormat outputAudioFormat = new AudioFormat(testContainer, testCodec,
                testBigEndian, testBitDepth, testBitRate, testFrequency)

        inputs.add(nullContainerAudioFormat)
        inputs.add(nullCodecAudioFormat)
        inputs.add(nullBitDepthAudioFormat)
        inputs.add(nullBitRateAudioFormat)
        inputs.add(nullFrequencyAudioFormat)

        outputs.add(outputAudioFormat)

        AudioFormat bestMatch = AudioFormat.getBestMatch(inputs, outputs)
        assertThat "The best match for the audio format was not as expected",
                bestMatch,
                is(nullValue())
    }

    @Test
    public void 'wave container is preferred when determinig a best match'(){
        Set<AudioFormat> audioFormats = new HashSet<>()

        AudioFormat waveContainerAudioFormat = new AudioFormat(testContainer, testCodec,
                true, testBitDepth, testBitRate, testFrequency)
        AudioFormat oggContainerAudioFormat = new AudioFormat(AudioFormat.CONTAINER_OGG, testCodec,
                true, testBitDepth, testBitRate, testFrequency)

        audioFormats.add(waveContainerAudioFormat)
        audioFormats.add(oggContainerAudioFormat)

        AudioFormat preferredFormat = AudioFormat.getPreferredFormat(audioFormats)
        assertThat "The best match for the audio format was not as expected",
                preferredFormat,
                is(waveContainerAudioFormat)
    }

    @Test
    public void 'the compatible wave container is the best match'(){
        Set<AudioFormat> inputs = new HashSet<>()
        Set<AudioFormat> outputs = new HashSet<>()

        AudioFormat alawAudioFormat = new AudioFormat(testContainer, AudioFormat.CODEC_PCM_ALAW,
                true, testBitDepth, testBitRate, testFrequency)
        AudioFormat ulawAudioFormat = new AudioFormat(testContainer, AudioFormat.CODEC_PCM_ULAW,
                true, testBitDepth, testBitRate, testFrequency)

        inputs.add(alawAudioFormat)
        inputs.add(ulawAudioFormat)

        outputs.add(alawAudioFormat)

        AudioFormat bestMatch = AudioFormat.getBestMatch(inputs, outputs)
        assertThat "The best match for the audio format was not as expected",
                bestMatch,
                is(alawAudioFormat)
    }

    @Test
    public void 'there is no best match if no wave container'(){
        Set<AudioFormat> inputs = new HashSet<>()
        Set<AudioFormat> outputs = new HashSet<>()

        AudioFormat oggContainerAudioFormat = new AudioFormat(AudioFormat.CONTAINER_OGG, testCodec,
                true, testBitDepth, testBitRate, testFrequency)
        AudioFormat nonContainerAudioFormat = new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3,
                true, testBitDepth, testBitRate, testFrequency)


        inputs.add(oggContainerAudioFormat)
        inputs.add(nonContainerAudioFormat)

        outputs.add(oggContainerAudioFormat)
        outputs.add(nonContainerAudioFormat)

        AudioFormat bestMatch = AudioFormat.getBestMatch(inputs, outputs)
        assertThat "The best match for the audio format was not as expected",
                bestMatch,
                is(nullValue())
    }
}
