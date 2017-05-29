/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio.test.fake

import org.eclipse.smarthome.core.audio.AudioFormat
import org.eclipse.smarthome.core.audio.AudioSink
import org.eclipse.smarthome.core.audio.AudioStream
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException
import org.eclipse.smarthome.core.library.types.PercentType

public class AudioSinkFake implements AudioSink{
    public AudioStream audioStream
    public AudioFormat audioFormat
    public boolean isStreamProcessed = false
    public PercentType volume
    public boolean isIOExceptionExpected = false
    public boolean isUnsupportedAudioFormatExceptionExpected = false

    @Override
    public String getId() {
        return "testSinkId";
    }

    @Override
    public String getLabel(Locale locale) {
        return "testSinkLabel";
    }

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        if(isUnsupportedAudioFormatExceptionExpected){
            throw new UnsupportedAudioFormatException("Expected audio format exception", null)
        }
        this.audioStream = audioStream
        audioFormat =  audioStream.getFormat()
        isStreamProcessed = true
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        return null;
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
