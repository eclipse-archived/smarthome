/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.util.Set;

import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * This service provides functionality around audio services and is the central service to be used directly by others.
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - removed unwanted dependencies
 */
public interface AudioManager {

    /**
     * Name of the sub-directory of the config folder, holding sound files.
     */
    static final String SOUND_DIR = "sounds";

    /**
     * Plays the passed audio stream using the default audio sink.
     *
     * @param audioStream The audio stream to play
     */
    void play(AudioStream audioStream);

    /**
     * Plays the passed audio stream on the given sink.
     *
     * @param audioStream The audio stream to play
     * @param sinkId The id of the audio sink to use or null
     */
    void play(AudioStream audioStream, String sinkId);

    /**
     * Plays an audio file from the "sounds" folder using the default audio sink.
     *
     * @throws AudioException in case the file does not exist or cannot be opened
     */
    void playFile(String fileName) throws AudioException;

    /**
     * Plays an audio file from the "sounds" folder using the given audio sink.
     *
     * @throws AudioException in case the file does not exist or cannot be opened
     */
    void playFile(String fileName, String sink) throws AudioException;

    /**
     * Stream audio from the passed url using the default audio sink.
     *
     * @throws AudioException in case the url stream cannot be opened
     */
    void stream(String url) throws AudioException;

    /**
     * Stream audio from the passed url to the given sink
     *
     * @param url The url to stream from or null if streaming should be stopped
     * @param sinkId The id of the audio sink to use or null
     * @throws AudioException in case the url stream cannot be opened
     */
    void stream(String url, String sinkId) throws AudioException;

    /**
     * Retrieves the current volume of a sink
     *
     * @param sinkId the sink to get the volume for
     * @return the volume as a value between 0 and 100
     */
    PercentType getVolume(String sinkId);

    /**
     * Sets the volume for a sink.
     *
     * @param volume the volume to set as a value between 0 and 100
     * @param sinkId the sink to set the volume
     */
    void setVolume(PercentType volume, String sinkId);

    /**
     * Retrieves an AudioSource.
     * If a default name is configured and the service available, this is returned. If no default name is configured,
     * the first available service is returned, if one exists. If no service with the default name is found, null is
     * returned.
     *
     * @return an AudioSource or null, if no service is available or if a default is configured, but no according
     *         service is found
     */
    AudioSource getSource();

    /**
     * Retrieves an AudioSink.
     * If a default name is configured and the service available, this is returned. If no default name is configured,
     * the first available service is returned, if one exists. If no service with the default name is found, null is
     * returned.
     *
     * @return an AudioSink or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    AudioSink getSink();

    /**
     * Retrieves the ids of all sources
     *
     * @return ids of all sources
     */
    Set<String> getSourceIds();

    /**
     * Retrieves the ids of all sinks
     *
     * @return ids of all sources
     */
    Set<String> getSinkIds();

    /**
     * Get a list of source ids that match a given pattern
     *
     * @param pattern pattern to search, can include `*` and `?` placeholders
     * @return ids of matching sources
     */
    Set<String> getSourceIds(String pattern);

    /**
     * Retrieves the sink for a given id
     *
     * @param sinkId the id of the sink or null for the default
     * @return the sink instance for the id or the default sink
     */
    AudioSink getSink(String sinkId);

    /**
     * Get a list of sink ids that match a given pattern
     *
     * @param pattern pattern to search, can include `*` and `?` placeholders
     * @return ids of matching sinks
     */
    Set<String> getSinks(String pattern);

}