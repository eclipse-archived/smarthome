package org.eclipse.smarthome.io.sound.file;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.smarthome.core.audio.AudioFormat;

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
