/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.webaudio.internal;

import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioHTTPServer;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream;
import org.eclipse.smarthome.core.audio.URLAudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.events.EventPublisher;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an audio sink that publishes an event through SSE and temporarily serves the stream via HTTP for web players
 * to pick it up.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class SSEAudioSink implements AudioSink {

    private final Logger logger = LoggerFactory.getLogger(SSEAudioSink.class);

    private static final HashSet<AudioFormat> supportedFormats = new HashSet<>();

    static {
        supportedFormats.add(AudioFormat.WAV);
        supportedFormats.add(AudioFormat.MP3);
    }

    private AudioHTTPServer audioHTTPServer;

    private EventPublisher eventPublisher;

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        logger.debug("Received audio stream of format {}", audioStream.getFormat());
        if (audioStream instanceof URLAudioStream) {
            // it is an external URL, so we can directly pass this on.
            URLAudioStream urlAudioStream = (URLAudioStream) audioStream;
            sendEvent(urlAudioStream.getURL());
            IOUtils.closeQuietly(audioStream);
        } else {
            // we serve it on our own HTTP server
            if (audioStream instanceof FixedLengthAudioStream) {
                // we need to serve it for a while and make it available to multiple clients, hence
                // only FixedLengthAudioStreams are supported
                String url = audioHTTPServer.serve((FixedLengthAudioStream) audioStream, 10).toString();
                sendEvent(url);
            } else {
                logger.warn("Only FixedLengthAudioStream are supported for the web audio sink.");
                IOUtils.closeQuietly(audioStream);
            }
        }
    }

    private void sendEvent(String url) {
        PlayURLEvent event = WebAudioEventFactory.createPlayURLEvent(url);
        eventPublisher.post(event);
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return supportedFormats;
    }

    @Override
    public String getId() {
        return "webaudio";
    }

    @Override
    public String getLabel(Locale locale) {
        return "Web Audio";
    }

    @Override
    public PercentType getVolume() throws IOException {
        return PercentType.HUNDRED;
    }

    @Override
    public void setVolume(final PercentType volume) throws IOException {
        throw new IOException("Web Audio sink does not support volume level changes.");
    }

    protected void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    protected void unsetEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = null;
    }

    protected void setAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = audioHTTPServer;
    }

    protected void unsetAudioHTTPServer(AudioHTTPServer audioHTTPServer) {
        this.audioHTTPServer = null;
    }

}
