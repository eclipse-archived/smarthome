/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * This is an AudioStream from an audio file
 *
 * @author Karel Goderis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to take a file as input
 */
public class FileAudioStream extends FixedLengthAudioStream {

    private File file;
    private AudioFormat audioFormat;
    private InputStream inputStream;
    private long length;

    public FileAudioStream(File file) throws AudioException {
        this.file = file;
        this.inputStream = getInputStream(file);
        this.audioFormat = getAudioFormat(file);
        this.length = file.length();
    }

    private static AudioFormat getAudioFormat(File file) throws AudioException {
        if (file.getName().toLowerCase().endsWith(".wav")) {
            return new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, false, 16, 705600, 44100L);
        } else if (file.getName().toLowerCase().endsWith(".mp3")) {
            return new AudioFormat(AudioFormat.CONTAINER_NONE, AudioFormat.CODEC_MP3, null, null, null, null);
        } else {
            throw new AudioException("Unsupported file extension!");
        }
    }

    private static InputStream getInputStream(File file) throws AudioException {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new AudioException("File '" + file.getName() + "' not found!");
        }
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        super.close();
    }

    @Override
    public long length() {
        return this.length;
    }

    @Override
    public synchronized void reset() throws IOException {
        IOUtils.closeQuietly(inputStream);
        try {
            inputStream = getInputStream(file);
        } catch (AudioException e) {
            throw new IOException("Cannot reset file input stream: " + e.getMessage(), e);
        }
    }

    @Override
    public InputStream getClonedStream() throws AudioException {
        return getInputStream(file);
    }
}
