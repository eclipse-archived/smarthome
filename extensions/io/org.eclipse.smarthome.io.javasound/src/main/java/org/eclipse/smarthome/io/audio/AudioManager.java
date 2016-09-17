/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.io.audio.file.FileAudioSource;
import org.eclipse.smarthome.io.audio.stream.StreamAudioSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service provides functionality around audio services and is the central service to be used directly by others.
 *
 * @author Karel Goderis - Initial contribution and API
 */
public class AudioManager {

    public static final String THREAD_POOL_NAME = "audio";

    // constants for the configuration properties
    private static final String CONFIG_DEFAULT_SINK = "defaultSink";
    private static final String CONFIG_DEFAULT_SOURCE = "defaultSource";

    private final Logger logger = LoggerFactory.getLogger(AudioManager.class);

    // service maps
    private Map<String, AudioSource> audioSources = new HashMap<>();
    private Map<String, AudioSink> audioSinks = new HashMap<>();

    /**
     * default settings filled through the service configuration
     */
    private String defaultSource = null;
    private String defaultSink = null;

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
     * Plays the passed filename using the default audio sink.
     *
     * @param filename The filename to play
     */
    public void play(String filename) {
        play(filename, null);
    }

    /**
     * Plays the passed filename
     *
     * @param filename The filename to play
     * @param sinkId The id of the audio sink to use or null
     */
    public void play(String filename, String sinkId) {

        AudioSource source = audioSources.get(filename);
        if (source == null) {
            source = new FileAudioSource(filename);
        }

        if (source != null) {
            AudioSink sink = null;
            if (sinkId == null) {
                sink = getSink();
            } else {
                sink = audioSinks.get(sinkId);
            }

            if (sink != null) {
                AudioFormat audioFormat = getBestMatch(source.getSupportedFormats(), sink.getSupportedFormats());
                if (audioFormat != null) {
                    AudioStream audioStream = null;
                    try {
                        audioStream = source.getInputStream();
                    } catch (AudioException e1) {
                        logger.error("Error getting the input stream '{}': {}", source, e1.getMessage());
                    }

                    if (audioStream != null) {
                        try {
                            sink.process(audioStream);
                        } catch (UnsupportedAudioFormatException e) {
                            logger.error("Error playing '{}': {}", filename, e.getMessage());
                        }
                    }
                } else {
                    logger.warn("No compatible audio format found for source '{}' and sink '{}'", source.getId(),
                            sink.getId());
                }
            }
        }

    }

    /**
     * Stream from the passed filename using the default audio sink.
     *
     */
    public void stream(String url) {
        stream(url, null);
    }

    /**
     * Stream from the passed filename
     *
     * @param url The url to stream from
     * @param sinkId The id of the audio sink to use or null
     */
    public void stream(String url, String sinkId) {

        AudioSource source = new StreamAudioSource(url);

        if (source != null) {
            AudioSink sink = null;
            if (sinkId == null) {
                sink = getSink();
            } else {
                sink = audioSinks.get(sinkId);
            }

            if (sink != null) {
                AudioFormat audioFormat = getBestMatch(source.getSupportedFormats(), sink.getSupportedFormats());
                if (audioFormat != null) {
                    AudioStream audioStream = null;
                    try {
                        audioStream = source.getInputStream();
                    } catch (AudioException e1) {
                        logger.error("Error getting the input stream '{}': {}", source, e1.getMessage());
                    }

                    if (audioStream != null) {
                        try {
                            sink.process(audioStream);
                        } catch (UnsupportedAudioFormatException e) {
                            logger.error("Error playing '{}': {}", url, e.getMessage());
                        }
                    }
                } else {
                    logger.warn("No compatible audio format found for source '{}' and sink '{}'", source.getId(),
                            sink.getId());
                }
            }
        }

    }

    public float getVolume(String sinkId) {
        AudioSink sink = null;
        if (sinkId == null) {
            sink = getSink();
        } else {
            sink = audioSinks.get(sinkId);
        }

        if (sink != null) {
            try {
                return sink.getVolume();
            } catch (IOException e) {
                logger.error("An exception occured while getting the volume of sink {} : '{}'", sink.getId(),
                        e.getMessage());
            }
        }

        return 0;

    }

    public void setVolume(String sinkId, float volume) {
        AudioSink sink = null;
        if (sinkId == null) {
            sink = getSink();
        } else {
            sink = audioSinks.get(sinkId);
        }

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
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
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
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
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

    public Set<String> getSources() {
        return audioSources.keySet();
    }

    public Set<String> getSources(String pattern) {
        String regex = pattern.replace("?", ".?").replace("*", ".*?");
        Set<String> matchedSources = new HashSet<String>();

        for (String aSource : audioSinks.keySet()) {
            if (aSource.matches(regex)) {
                matchedSources.add(aSource);
            }
        }

        return matchedSources;
    }

    public Set<String> getSinks() {
        return audioSinks.keySet();
    }

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
        logger.debug("Adding an AudioSource : '{}'", audioSource.getId());
        this.audioSources.put(audioSource.getId(), audioSource);
    }

    protected void removeAudioSource(AudioSource audioSource) {
        logger.debug("Removing an AudioSource : '{}'", audioSource.getId());
        this.audioSources.remove(audioSource.getId());
    }

    protected void addAudioSink(AudioSink audioSink) {
        logger.debug("Adding an AudioSink : '{}'", audioSink.getId());
        this.audioSinks.put(audioSink.getId(), audioSink);
    }

    protected void removeAudioSink(AudioSink audioSink) {
        logger.debug("Removing an AudioSink : '{}'", audioSink.getId());
        this.audioSinks.remove(audioSink.getId());
    }
}
