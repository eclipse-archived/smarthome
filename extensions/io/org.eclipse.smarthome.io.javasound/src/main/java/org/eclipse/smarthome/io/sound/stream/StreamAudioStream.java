package org.eclipse.smarthome.io.sound.stream;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.core.audio.AudioFormat;

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
