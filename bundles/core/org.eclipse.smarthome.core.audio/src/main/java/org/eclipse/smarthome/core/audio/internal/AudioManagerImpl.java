/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.ConfigOptionProvider;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FileAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This service provides functionality around audio services and is the central service to be used directly by others.
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - removed unwanted dependencies
 * @author Christoph Weitkamp - Added getSupportedStreams() and UnsupportedAudioStreamException
 * 
 */
public class AudioManagerImpl implements AudioManager, ConfigOptionProvider {

    // constants for the configuration properties
    private static final String CONFIG_URI = "system:audio";
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

    @Override
    public void play(AudioStream audioStream) {
        play(audioStream, null);
    }

    @Override
    public void play(AudioStream audioStream, String sinkId) {
        if (audioStream != null) {
            AudioSink sink = getSink(sinkId);

            if (sink != null) {
                try {
                    sink.process(audioStream);
                } catch (UnsupportedAudioFormatException | UnsupportedAudioStreamException e) {
                    logger.error("Error playing '{}': {}", audioStream.toString(), e.getMessage());
                }
            } else {
                logger.warn("Failed playing audio stream '{}' as no audio sink was found.", audioStream.toString());
            }
        }
    }

    @Override
    public void playFile(String fileName) throws AudioException {
        playFile(fileName, null);
    }

    @Override
    public void playFile(String fileName, String sink) throws AudioException {
        File file = new File(
                ConfigConstants.getConfigFolder() + File.separator + SOUND_DIR + File.separator + fileName);
        FileAudioStream is = new FileAudioStream(file);
        play(is, sink);
    }

    @Override
    public void stream(String url) throws AudioException {
        stream(url, null);
    }

    @Override
    public void stream(String url, String sinkId) throws AudioException {
        AudioStream audioStream = url != null ? new URLAudioStream(url) : null;
        AudioSink sink = getSink(sinkId);

        if (sink != null) {
            try {
                sink.process(audioStream);
            } catch (UnsupportedAudioFormatException | UnsupportedAudioStreamException e) {
                logger.error("Error playing '{}': {}", url, e.getMessage());
            }
        }
    }

    @Override
    public PercentType getVolume(String sinkId) {
        AudioSink sink = getSink(sinkId);

        if (sink != null) {
            try {
                return sink.getVolume();
            } catch (IOException e) {
                logger.error("An exception occurred while getting the volume of sink {} : '{}'", sink.getId(),
                        e.getMessage());
            }
        }

        return PercentType.ZERO;
    }

    @Override
    public void setVolume(PercentType volume, String sinkId) {
        AudioSink sink = getSink(sinkId);

        if (sink != null) {
            try {
                sink.setVolume(volume);
            } catch (IOException e) {
                logger.error("An exception occurred while setting the volume of sink {} : '{}'", sink.getId(),
                        e.getMessage());
            }
        }
    }

    @Override
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

    @Override
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

    @Override
    public Set<String> getSourceIds() {
        return new HashSet<>(audioSources.keySet());
    }

    @Override
    public Set<String> getSinkIds() {
        return new HashSet<>(audioSinks.keySet());
    }

    @Override
    public Set<String> getSourceIds(String pattern) {
        String regex = pattern.replace("?", ".?").replace("*", ".*?");
        Set<String> matchedSources = new HashSet<String>();

        for (String aSource : audioSources.keySet()) {
            if (aSource.matches(regex)) {
                matchedSources.add(aSource);
            }
        }

        return matchedSources;
    }

    @Override
    public AudioSink getSink(String sinkId) {
        AudioSink sink = null;
        if (sinkId == null) {
            sink = getSink();
        } else {
            sink = audioSinks.get(sinkId);
        }
        return sink;
    }

    @Override
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

    @Override
    public Collection<ParameterOption> getParameterOptions(URI uri, String param, Locale locale) {
        if (uri.toString().equals(CONFIG_URI)) {
            if (CONFIG_DEFAULT_SOURCE.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (AudioSource source : audioSources.values()) {
                    ParameterOption option = new ParameterOption(source.getId(), source.getLabel(locale));
                    options.add(option);
                }
                return options;
            } else if (CONFIG_DEFAULT_SINK.equals(param)) {
                List<ParameterOption> options = new ArrayList<>();
                for (AudioSink sink : audioSinks.values()) {
                    ParameterOption option = new ParameterOption(sink.getId(), sink.getLabel(locale));
                    options.add(option);
                }
                return options;
            }
        }
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
