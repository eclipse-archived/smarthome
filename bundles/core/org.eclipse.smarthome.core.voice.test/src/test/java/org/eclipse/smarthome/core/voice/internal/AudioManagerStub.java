/**
 * Copyright (c) 2014,2017 Contributors to the Eclipse Foundation
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

import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.library.types.PercentType;

/**
 * Only the get source and get sink methods are used in the tests.
 *
 * @author Velin Yordanov - initial contribution
 *
 */
public class AudioManagerStub implements AudioManager {
    private SinkStub sink = new SinkStub();
    private AudioSourceStub source = new AudioSourceStub();

    @Override
    public AudioSource getSource() {
        return source;
    }

    @Override
    public AudioSink getSink() {
        return sink;
    }

    @Override
    public void play(AudioStream audioStream) {

    }

    @Override
    public void play(AudioStream audioStream, String sinkId) {

    }

    @Override
    public void playFile(String fileName) throws AudioException {

    }

    @Override
    public void playFile(String fileName, String sink) throws AudioException {

    }

    @Override
    public void stream(String url) throws AudioException {

    }

    @Override
    public void stream(String url, String sinkId) throws AudioException {

    }

    @Override
    public PercentType getVolume(String sinkId) {
        return null;
    }

    @Override
    public void setVolume(PercentType volume, String sinkId) {

    }

    @Override
    public Set<String> getSourceIds() {
        return null;
    }

    @Override
    public Set<String> getSinkIds() {
        return null;
    }

    @Override
    public Set<String> getSourceIds(String pattern) {
        return null;
    }

    @Override
    public AudioSink getSink(String sinkId) {
        return null;
    }

    @Override
    public Set<String> getSinks(String pattern) {
        return null;
    }

}
