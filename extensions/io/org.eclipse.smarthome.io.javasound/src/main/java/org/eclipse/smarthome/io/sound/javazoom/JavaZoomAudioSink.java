/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.sound.javazoom;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * This is an audio sink that is registered as a service, which can play wave files to the hosts outputs (e.g. speaker,
 * line-out).
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class JavaZoomAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(JavaZoomAudioSink.class);

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {

        if (this.getSupportedFormats().contains(audioStream.getFormat())) {

            AudioPlayer audioPlayer = new AudioPlayer(audioStream);
            audioPlayer.start();
            try {
                audioPlayer.join();
            } catch (InterruptedException e) {
                logger.error("Playing audio has been interrupted.");
            }
        } else {
            logger.warn("Incompatible audio format : '{}'", audioStream.getFormat());
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        AudioFormat format = new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, null, null, null, null);
        return Collections.singleton(format);
    }

    @Override
    public String getId() {
        return "javazoom";
    }

    @Override
    public String getLabel(Locale locale) {
        return "MP3 Player";
    }

    class AudioPlayer extends Thread {

        private AudioStream audioStream;

        public AudioPlayer(AudioStream audioStream) {
            this.audioStream = audioStream;
        }

        @Override
        public void run() {

            final PipedOutputStream source = new PipedOutputStream();
            PipedInputStream sink = new PipedInputStream();

            try {
                sink.connect(source);
            } catch (IOException e) {
                logger.error("An exception occurred while feeding the audio source to the mp3 player : '{}'",
                        e.getMessage());
            }

            Producer p = new Producer(source);
            p.start();
            BufferedInputStream buffer = new BufferedInputStream(sink);
            Consumer c = new Consumer(buffer);
            c.start();

            try {
                p.join();
                c.join();
            } catch (InterruptedException e) {
                logger.error("Playing audio has been interrupted.");
            }

            try {
                sink.close();
            } catch (IOException e) {
                logger.error("An exception occurred while closing the audio sink: '{}'", e.getMessage());
            }

        }

        class Producer extends Thread {

            OutputStream source;

            Producer(OutputStream source) {
                this.source = source;
            }

            @Override
            public void run() {

                int nRead = 0;
                byte[] abData = new byte[65532]; // needs to be a multiple of 4 and 6, to support both 16 and 24 bit
                                                 // stereo
                try {
                    while (-1 != nRead) {
                        nRead = audioStream.read(abData, 0, abData.length);
                        if (nRead >= 0) {
                            source.write(abData, 0, nRead);
                        }
                    }
                } catch (IOException e) {
                    logger.error("An exception occurred while playing audio: '{}'", e.getMessage());
                }

                try {
                    source.flush();
                    source.close();
                } catch (Exception e) {
                    logger.error("An exception occurred while closing the audio source: '{}'", e.getMessage());
                }

            }
        }

        class Consumer extends Thread {

            private InputStream is;

            Consumer(InputStream is) {
                this.is = is;
            }

            @Override
            public void run() {
                Player player;
                try {
                    player = new Player(is);
                    player.play();
                } catch (JavaLayerException e1) {
                    logger.error("An exception occurred while playing an mp3 file : '{}'", e1.getMessage());
                }
            }
        }
    }
}
