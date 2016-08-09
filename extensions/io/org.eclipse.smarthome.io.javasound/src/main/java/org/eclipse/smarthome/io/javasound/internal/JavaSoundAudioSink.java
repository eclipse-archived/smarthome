/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.javasound.internal;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an audio sink that is registered as a service, which can play wave files to the hosts outputs (e.g. speaker,
 * line-out).
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class JavaSoundAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(JavaSoundAudioSink.class);

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        AudioPlayer audioPlayer = new AudioPlayer(audioStream);
        audioPlayer.start();
        try {
            audioPlayer.join();
        } catch (InterruptedException e) {
            logger.error("Playing audio has been interrupted.");
        }
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        // we accept anything that is WAVE with signed PCM codec
        AudioFormat format = new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, null, null,
                null);
        return Collections.singleton(format);
    }

    @Override
    public String getId() {
        return "javasound";
    }

    @Override
    public String getLabel(Locale locale) {
        return "System Speaker";
    }

}
