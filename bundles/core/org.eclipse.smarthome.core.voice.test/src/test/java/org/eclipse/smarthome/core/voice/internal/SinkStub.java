/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import java.io.IOException;
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
 *
 * @author Velin Yordanov - migrated from groovy to java
 *
 */
public class SinkStub implements AudioSink {

    private boolean isStreamProcessed;
    private boolean isUnsupportedAudioFormatExceptionExpected;
    private Set<AudioFormat> supportedFormats = new HashSet<AudioFormat>();

    private static final String SINK_STUB_ID = "sinkStubID";
    private static final String SINK_STUB_LABEL = "sinkStubLabel";

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
        supportedFormats.add(AudioFormat.MP3);
        supportedFormats.add(AudioFormat.WAV);
        return supportedFormats;
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
        // this method will not be used in the tests
        return null;
    }
}
