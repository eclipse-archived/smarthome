/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.voice.mactts.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a TTS service implementation for Mac OS, which simply uses the "say" command from the OS.
 *
 * @author Kai Kreuzer - Initial contribution and API
 * @author Pauli Antilla
 * @author Kelly Davis
 */
public class MacTTSService implements TTSService {

    private final Logger logger = LoggerFactory.getLogger(MacTTSService.class);

    /**
     * Set of supported voices
     */
    private final Set<Voice> voices = initVoices();

    /**
     * Set of supported audio formats
     */
    private final Set<AudioFormat> audioFormats = initAudioFormats();

    @Override
    public Set<Voice> getAvailableVoices() {
        return this.voices;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return this.audioFormats;
    }

    @Override
    public AudioStream synthesize(String text, Voice voice, AudioFormat requestedFormat) throws TTSException {
        // Validate arguments
        if ((null == text) || text.isEmpty()) {
            throw new TTSException("The passed text is null or empty");
        }
        if (!this.voices.contains(voice)) {
            throw new TTSException("The passed voice is unsupported");
        }
        boolean isAudioFormatSupported = false;
        for (AudioFormat currentAudioFormat : this.audioFormats) {
            if (currentAudioFormat.isCompatible(requestedFormat)) {
                isAudioFormatSupported = true;
                break;
            }
        }
        if (!isAudioFormatSupported) {
            throw new TTSException("The passed AudioFormat is unsupported");
        }

        try {
            return new MacTTSAudioStream(text, voice, requestedFormat);
        } catch (AudioException e) {
            throw new TTSException(e);
        }
    }

    /**
     * Initializes this.voices
     *
     * @return The voices of this instance
     */
    private final Set<Voice> initVoices() {
        Set<Voice> voices = new HashSet<Voice>();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        try {
            Process process = Runtime.getRuntime().exec("say -v ?");
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);

            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                voices.add(new MacTTSVoice(nextLine));
            }
        } catch (IOException e) {
            logger.error("Error while executing the 'say -v ?' command: " + e.getMessage());
        } finally {
            IOUtils.closeQuietly(bufferedReader);
        }
        return voices;
    }

    /**
     * Initializes this.audioFormats
     *
     * @return The audio formats of this instance
     */
    private final Set<AudioFormat> initAudioFormats() {
        AudioFormat audioFormat = new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16,
                null, (long) 44100);
        return Collections.singleton(audioFormat);
    }

    @Override
    public String getId() {
        return "mactts";
    }

    @Override
    public String getLabel(Locale locale) {
        return "MacOS TTS";
    }

}
