/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.util.Locale;
import java.util.Set;

/**
 * This is an audio source, which can provide a continuous live stream of audio.
 * Its main use is for microphones and other "line-in" sources and it can be registered as a service in order to make
 * it available throughout the system.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public interface AudioSource {

    /**
     * Returns a simple string that uniquely identifies this service
     *
     * @return an id that identifies this service
     */
    public String getId();

    /**
     * Returns a localized human readable label that can be used within UIs.
     *
     * @param locale the locale to provide the label for
     * @return a localized string to be used in UIs
     */
    public String getLabel(Locale locale);

    /**
     * Obtain the audio formats supported by this TTSService
     *
     * @return The audio formats supported by this service
     */
    public Set<AudioFormat> getSupportedFormats();

    /**
     * Gets InputStream for reading audio data in supported audio format
     *
     * @return InputStream for reading audio data
     * @throws AudioException If problem occurs obtaining InputStream
     */
    abstract public AudioStream getInputStream() throws AudioException;

}
