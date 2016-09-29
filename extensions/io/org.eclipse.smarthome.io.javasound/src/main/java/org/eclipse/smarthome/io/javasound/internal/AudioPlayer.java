/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.javasound.internal;

import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Line.Info;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import org.eclipse.smarthome.core.audio.AudioStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class that plays an AudioStream through the Java sound API
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to use AudioStream and logging
 *
 */
public class AudioPlayer extends Thread {

    private final Logger logger = LoggerFactory.getLogger(AudioPlayer.class);

    /**
     * The AudioStream to play
     */
    private final AudioStream audioStream;

    /**
     * Constructs an AudioPlayer to play the passed AudioSource
     *
     * @param audioSource The AudioSource to play
     */
    public AudioPlayer(AudioStream audioStream) {
        this.audioStream = audioStream;
    }

    /**
     * This method plays the contained AudioSource
     */
    @Override
    public void run() {
        SourceDataLine line;
        AudioFormat audioFormat = convertAudioFormat(this.audioStream.getFormat());
        if (audioFormat == null) {
            logger.warn("Audio format is unsupported or does not have enough details in order to be played");
            return;
        }
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
        try {
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open(audioFormat);
        } catch (Exception e) {
            logger.warn("No line found: {}", e.getMessage());
            logger.info("Available lines are:");
            Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo(); // get available mixers
            Mixer mixer = null;
            for (int cnt = 0; cnt < mixerInfo.length; cnt++) {
                mixer = AudioSystem.getMixer(mixerInfo[cnt]);
                Line.Info[] lineInfos = mixer.getSourceLineInfo();
                for (Info lineInfo : lineInfos) {
                    logger.info(lineInfo.toString());
                }
            }
            return;
        }
        line.start();
        int nRead = 0;
        byte[] abData = new byte[65532]; // needs to be a multiple of 4 and 6, to support both 16 and 24 bit stereo
        try {
            while (-1 != nRead) {
                nRead = audioStream.read(abData, 0, abData.length);
                if (nRead >= 0) {
                    line.write(abData, 0, nRead);
                }
            }
        } catch (IOException e) {
            logger.error("Error while playing audio: {}", e.getMessage());
            return;
        } finally {
            line.drain();
            line.close();
            try {
                audioStream.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * Converts a org.eclipse.smarthome.core.audio.AudioFormat
     * to a javax.sound.sampled.AudioFormat
     *
     * @param audioFormat The AudioFormat to convert
     * @return The corresponding AudioFormat
     */
    @SuppressWarnings("null")
    protected AudioFormat convertAudioFormat(org.eclipse.smarthome.core.audio.AudioFormat audioFormat) {
        AudioFormat.Encoding encoding = new AudioFormat.Encoding(audioFormat.getCodec());
        if (audioFormat.getCodec().equals(org.eclipse.smarthome.core.audio.AudioFormat.CODEC_PCM_SIGNED)) {
            encoding = AudioFormat.Encoding.PCM_SIGNED;
        } else if (audioFormat.getCodec().equals(org.eclipse.smarthome.core.audio.AudioFormat.CODEC_PCM_ULAW)) {
            encoding = AudioFormat.Encoding.ULAW;
        } else if (audioFormat.getCodec().equals(org.eclipse.smarthome.core.audio.AudioFormat.CODEC_PCM_ALAW)) {
            encoding = AudioFormat.Encoding.ALAW;
        }
        Float sampleRate = audioFormat.getFrequency() != null ? audioFormat.getFrequency().floatValue() : null;
        Integer sampleSizeInBits = audioFormat.getBitDepth() != null ? audioFormat.getBitDepth().intValue() : null;
        Integer channels = 1; // TODO: Is this always true?
        Integer frameSize = audioFormat.getBitDepth() != null ? audioFormat.getBitDepth().intValue() / 8 : null;
        // frameSize
        Float frameRate = (sampleRate != null && frameSize != null) ? (sampleRate / frameSize) : null;
        Boolean bigEndian = audioFormat.isBigEndian() != null ? audioFormat.isBigEndian().booleanValue() : null;
        try {
            return new AudioFormat(encoding, sampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
