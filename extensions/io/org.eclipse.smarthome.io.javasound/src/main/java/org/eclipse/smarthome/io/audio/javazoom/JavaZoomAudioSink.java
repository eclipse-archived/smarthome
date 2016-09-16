/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.javazoom;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.common.SafeMethodCaller;
import org.eclipse.smarthome.core.common.ThreadPoolManager;
import org.eclipse.smarthome.io.audio.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;

/**
 * This is an audio sink that is registered as a service, which can play mp3 files to the hosts outputs (e.g. speaker,
 * line-out).
 *
 * @author Karel Goderis - Initial contribution and API
 */
public class JavaZoomAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(JavaZoomAudioSink.class);

    private final static int TIME_OUT = 10000;

    @Override
    public String getId() {
        return "javazoom";
    }

    @Override
    public String getLabel(Locale locale) {
        return "MP3 Player";
    }

    @Override
    public void process(final AudioStream audioStream) throws UnsupportedAudioFormatException {

        if (this.getSupportedFormats().contains(audioStream.getFormat())) {

            ThreadPoolManager.getPool(AudioManager.THREAD_POOL_NAME).submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        SafeMethodCaller.call(new SafeMethodCaller.ActionWithException<Void>() {
                            @Override
                            public Void call() {

                                final PipedOutputStream source = new PipedOutputStream();
                                PipedInputStream sink = new PipedInputStream();

                                try {
                                    sink.connect(source);
                                } catch (IOException e) {
                                    logger.error(
                                            "An exception occurred while feeding the audio source to the mp3 player : '{}'",
                                            e.getMessage());
                                }

                                Producer p = new Producer(source, audioStream);
                                BufferedInputStream buffer = new BufferedInputStream(sink);
                                Consumer c = new Consumer(buffer);
                                Future<?> producerFuture = ThreadPoolManager.getPool(AudioManager.THREAD_POOL_NAME)
                                        .submit(p);
                                Future<?> consumerFuture = ThreadPoolManager.getPool(AudioManager.THREAD_POOL_NAME)
                                        .submit(c);

                                try {
                                    producerFuture.get();
                                    consumerFuture.get();
                                } catch (Exception e) {
                                    logger.error("An exception has occured while playing audio : '{}'", e.getMessage());
                                }

                                try {
                                    sink.close();
                                } catch (IOException e) {
                                    logger.error("An exception occurred while closing the audio sink: '{}'",
                                            e.getMessage());
                                }

                                return null;
                            }
                        }, TIME_OUT);
                    } catch (ExecutionException e) {
                        logger.error("An exception occurred wile playing an audiostream : '{}'", audioStream,
                                e.getMessage());
                    } catch (TimeoutException e) {
                        logger.error("A timeout occurred while playing an audiostream : '{}'", e.getMessage());
                    }
                }
            });

        } else {
            logger.warn("Incompatible audio format : '{}'", audioStream.getFormat());
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        AudioFormat format = new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, null, null, null, null);
        return Collections.singleton(format);
    }

    class Producer implements Runnable {

        OutputStream source;
        AudioStream audioStream;

        Producer(OutputStream source, AudioStream audioStream) {
            this.source = source;
            this.audioStream = audioStream;
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

    class Consumer implements Runnable {

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

    @Override
    public float getVolume() throws IOException {
        throw new IOException("Volume is not supported by this sink");
    }

    @Override
    public void setVolume(float volume) throws IOException {
        throw new IOException("Volume is not supported by this sink");
    }

}
