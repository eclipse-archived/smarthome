/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service provides functionality around audio services and is the central service to be used directly by others.
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - removed unwanted dependencies
 */
public class AudioManager {

    private static final String SOUND_DIR = "sounds";

    // constants for the configuration properties
    private static final String CONFIG_DEFAULT_SINK = "defaultSink";
    private static final String CONFIG_DEFAULT_SOURCE = "defaultSource";

    private final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    // service maps
    private Map<String, AudioSource> audioSources = new ConcurrentHashMap<>();
    private Map<String, AudioSink> audioSinks = new ConcurrentHashMap<>();

    /**
     * default settings filled through the service configuration
     */
    private String defaultSource;
    private String defaultSink;

    protected void activate(Map<String, Object> config) {
        modified(config);
    }

    protected void deactivate() {
    }

    protected void modified(Map<String, Object> config) {
        if (config != null) {
            this.defaultSource = config.containsKey(CONFIG_DEFAULT_SOURCE)
                    ? config.get(CONFIG_DEFAULT_SOURCE).toString() : null;
            this.defaultSink = config.containsKey(CONFIG_DEFAULT_SINK) ? config.get(CONFIG_DEFAULT_SINK).toString()
                    : null;
        }
    }

    /**
     * Plays the passed audio stream using the default audio sink.
     *
     * @param audioStream The audio stream to play
     */
    public void play(AudioStream audioStream) {
        play(audioStream, null);
    }

    /**
     * Plays the passed audio stream on the given sink.
     *
     * @param audioStream The audio stream to play
     * @param sinkId The id of the audio sink to use or null
     */
    public void play(AudioStream audioStream, String sinkId) {
        if (audioStream != null) {
            AudioSink sink = getSink(sinkId);

            if (sink != null) {
                try {
                    sink.process(audioStream);
                } catch (UnsupportedAudioFormatException e) {
                    logger.error("Error playing '{}': {}", audioStream.toString(), e.getMessage());
                }
            } else {
                logger.warn("Failed playing audio stream '{}' as no audio sink was found.", audioStream.toString());
            }
        }
    }

    /**
     * Plays an audio file from the "sounds" folder using the default audio sink.
     *
     * @throws AudioException in case the file does not exist or cannot be opened
     */
    public void playFile(String fileName) throws AudioException {
        playFile(fileName, null);
    }

    /**
     * Plays an audio file from the "sounds" folder using the given audio sink.
     *
     * @throws AudioException in case the file does not exist or cannot be opened
     */
    public void playFile(String fileName, String sink) throws AudioException {
        File file = new File(
                ConfigConstants.getConfigFolder() + File.separator + SOUND_DIR + File.separator + fileName);
        FileAudioStream is = new FileAudioStream(file);
        play(is, sink);
    }

    /**
     * Stream audio from the passed url using the default audio sink.
     *
     * @throws AudioException in case the url stream cannot be opened
     */
    public void stream(String url) throws AudioException {
        stream(url, null);
    }

    /**
     * Stream audio from the passed url to the given sink
     *
     * @param url The url to stream from or null if streaming should be stopped
     * @param sinkId The id of the audio sink to use or null
     * @throws AudioException in case the url stream cannot be opened
     */
    public void stream(String url, String sinkId) throws AudioException {
        AudioStream audioStream = url != null ? new URLAudioStream(url) : null;
        AudioSink sink = getSink(sinkId);

        if (sink != null) {
            try {
                sink.process(audioStream);
            } catch (UnsupportedAudioFormatException e) {
                logger.error("Error playing '{}': {}", url, e.getMessage());
            }
        }
    }

    /**
     * Retrieves the current volume of a sink
     *
     * @param sinkId the sink to get the volume for
     * @return the volume as a value between 0 and 100
     */
    public PercentType getVolume(String sinkId) {
        AudioSink sink = getSink(sinkId);

        if (sink != null) {
            try {
                return sink.getVolume();
            } catch (IOException e) {
                logger.error("An exception occured while getting the volume of sink {} : '{}'", sink.getId(),
                        e.getMessage());
            }
        }

        return PercentType.ZERO;
    }

    /**
     * Sets the volume for a sink.
     *
     * @param volume the volume to set as a value between 0 and 100
     * @param sinkId the sink to set the volume
     */
    public void setVolume(PercentType volume, String sinkId) {
        AudioSink sink = getSink(sinkId);

        if (sink != null) {
            try {
                sink.setVolume(volume);
            } catch (IOException e) {
                logger.error("An exception occured while setting the volume of sink {} : '{}'", sink.getId(),
                        e.getMessage());
            }
        }
    }

    /**
     * Retrieves an AudioSource.
     * If a default name is configured and the service available, this is returned. If no default name is configured,
     * the first available service is returned, if one exists. If no service with the default name is found, null is
     * returned.
     *
     * @return an AudioSource or null, if no service is available or if a default is configured, but no according
     *         service is found
     */
    public AudioSource getSource() {
        AudioSource source = null;
        if (defaultSource != null) {
            source = audioSources.get(defaultSource);
            if (source == null) {
                logger.warn("Default AudioSource service '{}' not available!", defaultSource);
            }
        } else if (!audioSources.isEmpty()) {
            source = audioSources.values().iterator().next();
        } else {
            logger.debug("No AudioSource service available!");
        }
        return source;
    }

    /**
     * Retrieves an AudioSink.
     * If a default name is configured and the service available, this is returned. If no default name is configured,
     * the first available service is returned, if one exists. If no service with the default name is found, null is
     * returned.
     *
     * @return an AudioSink or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    public AudioSink getSink() {
        AudioSink sink = null;
        if (defaultSink != null) {
            sink = audioSinks.get(defaultSink);
            if (sink == null) {
                logger.warn("Default AudioSink service '{}' not available!", defaultSink);
            }
        } else if (!audioSinks.isEmpty()) {
            sink = audioSinks.values().iterator().next();
        } else {
            logger.debug("No AudioSink service available!");
        }
        return sink;
    }

    /**
     * Retrieves the ids of all sources
     *
     * @return ids of all sources
     */
    public Set<String> getSourceIds() {
        return new HashSet<>(audioSources.keySet());
    }

    /**
     * Retrieves the ids of all sinks
     *
     * @return ids of all sources
     */
    public Set<String> getSinkIds() {
        return new HashSet<>(audioSinks.keySet());
    }

    /**
     * Get a list of source ids that match a given pattern
     *
     * @param pattern pattern to search, can include `*` and `?` placeholders
     * @return ids of matching sources
     */
    public Set<String> getSourceIds(String pattern) {
        String regex = pattern.replace("?", ".?").replace("*", ".*?");
        Set<String> matchedSources = new HashSet<String>();

        for (String aSource : audioSinks.keySet()) {
            if (aSource.matches(regex)) {
                matchedSources.add(aSource);
            }
        }

        return matchedSources;
    }

    /**
     * Retrieves the sink for a given id
     *
     * @param sinkId the id of the sink or null for the default
     * @return the sink instance for the id or the default sink
     */
    public AudioSink getSink(String sinkId) {
        AudioSink sink = null;
        if (sinkId == null) {
            sink = getSink();
        } else {
            sink = audioSinks.get(sinkId);
        }
        return sink;
    }

    /**
     * Get a list of sink ids that match a given pattern
     *
     * @param pattern pattern to search, can include `*` and `?` placeholders
     * @return ids of matching sinks
     */
    public Set<String> getSinks(String pattern) {
        String regex = pattern.replace("?", ".?").replace("*", ".*?");
        Set<String> matchedSinks = new HashSet<String>();

        for (String aSink : audioSinks.keySet()) {
            if (aSink.matches(regex)) {
                matchedSinks.add(aSink);
            }
        }

        return matchedSinks;
    }

    /**
     * Determines the best match between a list of audio formats supported by a source and a sink.
     *
     * @param inputs the supported audio formats of an audio source
     * @param outputs the supported audio formats of an audio sink
     * @return the best matching format or null, if source and sink are incompatible
     */
    public static AudioFormat getBestMatch(Set<AudioFormat> inputs, Set<AudioFormat> outputs) {
        AudioFormat preferredFormat = getPreferredFormat(inputs);
        for (AudioFormat output : outputs) {
            if (output.isCompatible(preferredFormat)) {
                return preferredFormat;
            } else {
                for (AudioFormat input : inputs) {
                    if (output.isCompatible(input)) {
                        return input;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the first concrete AudioFormat in the passed set or a preferred one
     * based on 16bit, 16KHz, big endian default
     *
     * @param audioFormats The AudioFormats from which to choose
     * @return The preferred AudioFormat. A passed concrete format is preferred adding
     *         default values to an abstract AudioFormat in the passed Set.
     */
    public static AudioFormat getPreferredFormat(Set<AudioFormat> audioFormats) {
        // Return the first concrete AudioFormat found
        for (AudioFormat currentAudioFormat : audioFormats) {
            // Check if currentAudioFormat is abstract
            if (null == currentAudioFormat.getCodec()) {
                continue;
            }
            if (null == currentAudioFormat.getContainer()) {
                continue;
            }
            if (null == currentAudioFormat.isBigEndian()) {
                continue;
            }
            if (null == currentAudioFormat.getBitDepth()) {
                continue;
            }
            if (null == currentAudioFormat.getBitRate()) {
                continue;
            }
            if (null == currentAudioFormat.getFrequency()) {
                continue;
            }

            // Prefer WAVE container
            if (!currentAudioFormat.getContainer().equals("WAVE")) {
                continue;
            }

            // As currentAudioFormat is concrete, use it
            return currentAudioFormat;
        }

        // There's no concrete AudioFormat so we must create one
        for (AudioFormat currentAudioFormat : audioFormats) {
            // Define AudioFormat to return
            AudioFormat format = currentAudioFormat;

            // Not all Codecs and containers can be supported
            if (null == format.getCodec()) {
                continue;
            }
            if (null == format.getContainer()) {
                continue;
            }

            // Prefer WAVE container
            if (!format.getContainer().equals(AudioFormat.CONTAINER_WAVE)) {
                continue;
            }

            // If required set BigEndian, BitDepth, BitRate, and Frequency to default values
            if (null == format.isBigEndian()) {
                format = new AudioFormat(format.getContainer(), format.getCodec(), new Boolean(true),
                        format.getBitDepth(), format.getBitRate(), format.getFrequency());
            }
            if (null == format.getBitDepth() || null == format.getBitRate() || null == format.getFrequency()) {
                // Define default values
                int defaultBitDepth = 16;
                long defaultFrequency = 16384;

                // Obtain current values
                Integer bitRate = format.getBitRate();
                Long frequency = format.getFrequency();
                Integer bitDepth = format.getBitDepth();

                // These values must be interdependent (bitRate = bitDepth * frequency)
                if (null == bitRate) {
                    if (null == bitDepth) {
                        bitDepth = new Integer(defaultBitDepth);
                    }
                    if (null == frequency) {
                        frequency = new Long(defaultFrequency);
                    }
                    bitRate = new Integer(bitDepth.intValue() * frequency.intValue());
                } else if (null == bitDepth) {
                    if (null == frequency) {
                        frequency = new Long(defaultFrequency);
                    }
                    bitDepth = new Integer(bitRate.intValue() / frequency.intValue());
                } else if (null == frequency) {
                    frequency = new Long(bitRate.longValue() / bitDepth.longValue());
                }

                format = new AudioFormat(format.getContainer(), format.getCodec(), format.isBigEndian(), bitDepth,
                        bitRate, frequency);
            }

            // Return preferred AudioFormat
            return format;
        }

        // Return null indicating failure
        return null;
    }

    protected void addAudioSource(AudioSource audioSource) {
        this.audioSources.put(audioSource.getId(), audioSource);
    }

    protected void removeAudioSource(AudioSource audioSource) {
        this.audioSources.remove(audioSource.getId());
    }

    protected void addAudioSink(AudioSink audioSink) {
        this.audioSinks.put(audioSink.getId(), audioSink);
    }

    protected void removeAudioSink(AudioSink audioSink) {
        this.audioSinks.remove(audioSink.getId());
    }
}
