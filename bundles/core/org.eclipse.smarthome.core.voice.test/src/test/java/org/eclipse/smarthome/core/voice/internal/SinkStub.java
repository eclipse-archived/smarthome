/**
 * Copyright (c) 2014,2018 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.smarthome.core.voice.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * An {@link AudioSink} stub used for the tests. Since the tests do not cover all the voice's features,
 * some of the methods are not needed. That's why their implementation is left empty.
 *
 * @author Mihaela Memova - initial contribution
 * @author Velin Yordanov - migrated from groovy to java
 */
public class SinkStub implements AudioSink {

    private boolean isStreamProcessed;
    private static final Set<AudioFormat> SUPPORTED_AUDIO_FORMATS = new HashSet<>();
    private static final Set<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = new HashSet<>();

    private static final String SINK_STUB_ID = "sinkStubID";
    private static final String SINK_STUB_LABEL = "sinkStubLabel";

    static {
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.MP3);

        SUPPORTED_AUDIO_STREAMS.add(AudioStream.class);
    }

    @Override
    public String getId() {
        return SINK_STUB_ID;
    }

    @Override
    public String getLabel(Locale locale) {
        return SINK_STUB_LABEL;
    }

    public boolean getIsStreamProcessed() {
        return isStreamProcessed;
    }

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        isStreamProcessed = true;
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return Collections.unmodifiableSet(SUPPORTED_AUDIO_FORMATS);
    }

    @Override
    public PercentType getVolume() throws IOException {
        // this method will no be used in the tests
        return null;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        // this method will not be used in the tests
    }

    public boolean isStreamProcessed() {
        return isStreamProcessed;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return Collections.unmodifiableSet(SUPPORTED_AUDIO_STREAMS);
    }
}
