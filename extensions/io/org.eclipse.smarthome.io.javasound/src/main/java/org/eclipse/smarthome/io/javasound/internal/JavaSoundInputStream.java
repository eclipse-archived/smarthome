/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.javasound.internal;
/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

import java.io.IOException;

import javax.sound.sampled.TargetDataLine;

import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioStream;

/**
 * This is an AudioStream from a Java sound API input
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class JavaSoundInputStream extends AudioStream {

    /**
     * TargetDataLine for the input
     */
    private final TargetDataLine input;
    private final AudioFormat format;

    /**
     * Constructs a JavaSoundInputStream with the passed input
     *
     * @param input The mic which data is pulled from
     */
    public JavaSoundInputStream(TargetDataLine input, AudioFormat format) {
        this.format = format;
        this.input = input;
        this.input.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];

        int bytesRead = read(b);

        if (-1 == bytesRead) {
            return bytesRead;
        }

        Byte bb = new Byte(b[0]);
        return bb.intValue();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return input.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return input.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        input.close();
    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }
}
