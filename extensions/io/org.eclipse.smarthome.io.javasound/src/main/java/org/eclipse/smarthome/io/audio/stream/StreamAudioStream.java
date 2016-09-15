/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.stream;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.core.audio.AudioFormat;

/**
 * This is an AudioStream from an URL-driven audio stream. Note that some Sinks, like Sonos, can directly handle URL
 * based streams, and therefore can/should call getURL() to get an direct reference to the URL
 *
 * @author Karel Goderis - Initial contribution and API
 *
 */
public class StreamAudioStream extends org.eclipse.smarthome.core.audio.AudioStream {

    private AudioFormat audioFormat;
    private InputStream inputStream;
    private String url;

    public StreamAudioStream(InputStream is, AudioFormat af, String url) {
        this.inputStream = is;
        this.audioFormat = af;
        this.url = url;
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    public String getURL() {
        return url;
    }

}
