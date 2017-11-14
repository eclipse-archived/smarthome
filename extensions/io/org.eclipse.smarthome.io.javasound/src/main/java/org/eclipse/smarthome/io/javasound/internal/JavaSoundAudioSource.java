/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.javasound.internal;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;

/**
 * This is an AudioSource from an input channel of the host.
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored and stabilized
 *
 */
public class JavaSoundAudioSource implements AudioSource {

    /**
     * Java Sound audio format
     */
    private final javax.sound.sampled.AudioFormat format = new javax.sound.sampled.AudioFormat(16000.0f, 16, 1, true,
            false);

    /**
     * AudioFormat of the JavaSoundAudioSource
     */
    private final AudioFormat audioFormat = convertAudioFormat(format);

    /**
     * TargetDataLine for the mic
     */
    private TargetDataLine microphone;

    /**
     * Constructs a JavaSoundAudioSource
     */
    public JavaSoundAudioSource() {
    }

    private void initMicrophone(javax.sound.sampled.AudioFormat format) throws AudioException {
        try {
            TargetDataLine microphone;
            microphone = AudioSystem.getTargetDataLine(format);

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            microphone = (TargetDataLine) AudioSystem.getLine(info);

            microphone.open(format);

            this.microphone = microphone;
        } catch (Exception e) {
            throw new AudioException("Error creating the audio input stream.", e);
        }
    }

    @Override
    public synchronized AudioStream getInputStream(AudioFormat expectedFormat) throws AudioException {
        if (!expectedFormat.isCompatible(audioFormat)) {
            throw new AudioException("Cannot produce streams in format " + expectedFormat);
        }
        if (microphone == null) {
            initMicrophone(format);
        }
        return new JavaSoundInputStream(this.microphone, audioFormat);
    }

    @Override
    public String toString() {
        return "javasound";
    }

    /**
     * Converts a javax.sound.sampled.AudioFormat to a org.eclipse.smarthome.core.audio.AudioFormat
     *
     * @param audioFormat the AudioFormat to convert
     * @return The converted AudioFormat
     */
    private static AudioFormat convertAudioFormat(javax.sound.sampled.AudioFormat audioFormat) {
        String container = AudioFormat.CONTAINER_WAVE;

        String codec = audioFormat.getEncoding().toString();

        Boolean bigEndian = new Boolean(audioFormat.isBigEndian());

        int frameSize = audioFormat.getFrameSize(); // In bytes
        int bitsPerFrame = frameSize * 8;
        Integer bitDepth = ((AudioSystem.NOT_SPECIFIED == frameSize) ? null : new Integer(bitsPerFrame));

        float frameRate = audioFormat.getFrameRate();
        Integer bitRate = ((AudioSystem.NOT_SPECIFIED == frameRate) ? null
                : new Integer((int) (frameRate * bitsPerFrame)));

        float sampleRate = audioFormat.getSampleRate();
        Long frequency = ((AudioSystem.NOT_SPECIFIED == sampleRate) ? null : new Long((long) sampleRate));

        return new AudioFormat(container, codec, bigEndian, bitDepth, bitRate, frequency);
    }

    @Override
    public String getId() {
        return "javasound";
    }

    @Override
    public String getLabel(Locale locale) {
        return "System Microphone";
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Collections.singleton(audioFormat);
    }

}
