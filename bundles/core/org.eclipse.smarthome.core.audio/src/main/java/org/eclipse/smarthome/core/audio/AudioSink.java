/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Definition of an audio output like headphones, a speaker or for writing to
 * a file / clip.
 *
 * @author Harald Kuhn - Initial API
 * @author Kelly Davis - Modified to match discussion in #584
 */
public interface AudioSink {

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
     * Processes the passed {@link AudioStream}
     *
     * If the passed {@link AudioStream} has a {@link AudioFormat} not supported by this instance,
     * an {@link UnsupportedAudioFormatException} is thrown. In case the audioStream is null, this should be interpreted
     * as a request to end any currently playing stream.
     *
     * @param audioStream the audio stream to play or null to keep quiet
     *
     * @throws UnsupportedAudioFormatException If audioStream format is not supported
     */
    void process(AudioStream audioStream) throws UnsupportedAudioFormatException;

    /**
     * Gets a set containing all supported audio formats
     *
     * @return A Set containing all supported audio formats
     */
    public Set<AudioFormat> getSupportedFormats();

    /**
     * Gets the volume
     *
     * @return a PercentType value between 0 and 100 representing the actual volume
     * @throws IOException if the volume can not be determined
     */
    public PercentType getVolume() throws IOException;

    /**
     * Sets the volume
     *
     * @param volume a PercentType value between 0 and 100 representing the desired volume
     * @throws IOException if the volume can not be set
     */
    public void setVolume(PercentType volume) throws IOException;
}
