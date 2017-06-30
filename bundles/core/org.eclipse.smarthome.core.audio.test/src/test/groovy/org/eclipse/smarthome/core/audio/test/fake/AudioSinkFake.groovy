/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio.test.fake

import java.util.Set

import org.eclipse.smarthome.core.audio.AudioFormat
import org.eclipse.smarthome.core.audio.AudioSink
import org.eclipse.smarthome.core.audio.AudioStream
import org.eclipse.smarthome.core.audio.FixedLengthAudioStream
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException
import org.eclipse.smarthome.core.audio.UnsupportedAudioStreamException
import org.eclipse.smarthome.core.audio.URLAudioStream
import org.eclipse.smarthome.core.library.types.PercentType

/**
 *
 * @author Christoph Weitkamp - Added examples for getSupportedFormats() and getSupportedStreams()
 *
 */
public class AudioSinkFake implements AudioSink {

    public AudioStream audioStream
    public AudioFormat audioFormat
    public boolean isStreamProcessed = false
    public PercentType volume
    public boolean isIOExceptionExpected = false
    public boolean isUnsupportedAudioFormatExceptionExpected = false
    public boolean isUnsupportedAudioStreamExceptionExpected = false

    private static final HashSet<AudioFormat> SUPPORTED_AUDIO_FORMATS = new HashSet<>();
    private static final HashSet<Class<? extends AudioStream>> SUPPORTED_AUDIO_STREAMS = new HashSet<>();

    static {
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.WAV);
        SUPPORTED_AUDIO_FORMATS.add(AudioFormat.MP3);

        SUPPORTED_AUDIO_STREAMS.add(URLAudioStream.class);
        SUPPORTED_AUDIO_STREAMS.add(FixedLengthAudioStream.class);
    }

    @Override
    public String getId() {
        return "testSinkId";
    }

    @Override
    public String getLabel(Locale locale) {
        return "testSinkLabel";
    }

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException, UnsupportedAudioStreamException {
        if(isUnsupportedAudioFormatExceptionExpected){
            throw new UnsupportedAudioFormatException("Expected audio format exception", null)
        }
        if(isUnsupportedAudioStreamExceptionExpected){
            throw new UnsupportedAudioStreamException("Expected audio stream exception", null)
        }
        this.audioStream = audioStream
        audioFormat =  audioStream.getFormat()
        isStreamProcessed = true
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return SUPPORTED_AUDIO_FORMATS;
    }

    @Override
    public Set<Class<? extends AudioStream>> getSupportedStreams() {
        return SUPPORTED_AUDIO_STREAMS;
    }

    @Override
    public PercentType getVolume() throws IOException {
        if(isIOExceptionExpected){
            throw new IOException()
        }
        return volume;
    }

    @Override
    public void setVolume(PercentType volume) throws IOException {
        if(isIOExceptionExpected){
            throw new IOException()
        }
        this.volume = volume
    }
}
