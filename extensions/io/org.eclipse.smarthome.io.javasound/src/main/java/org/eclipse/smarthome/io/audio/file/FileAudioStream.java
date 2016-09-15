/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.file;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.core.audio.AudioFormat;

/**
 * This is an AudioStream from an audio file
 *
 * @author Karel Goderis - Initial contribution and API
 *
 */
public class FileAudioStream extends org.eclipse.smarthome.core.audio.AudioStream {

    private AudioFormat audioFormat;
    private InputStream inputStream;

    public FileAudioStream(InputStream is, AudioFormat af) {
        this.inputStream = is;
        this.audioFormat = af;
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

}
