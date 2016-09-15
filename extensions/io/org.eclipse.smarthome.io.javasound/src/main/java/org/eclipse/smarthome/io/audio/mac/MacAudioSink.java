/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.mac;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;

/**
 * This is an audio sink that is registered as a service, which can control the volume of a macOS X host. Note that it
 * can not play audio, it is just for controlling the volume
 *
 * @author Karel Goderis - Initial contribution
 */
public class MacAudioSink implements AudioSink {

    @Override
    public String getId() {
        return "macosx";
    }

    @Override
    public String getLabel(Locale locale) {
        return "macOS X host controller";
    }

    @Override
    public void process(AudioStream audioStream) throws UnsupportedAudioFormatException {
        // Nothing to do here
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        // Nothing to do here
        return null;
    }

    @Override
    public float getVolume() throws IOException {
        if (System.getProperty("osgi.os").equals("macosx")) {
            Process p = Runtime.getRuntime()
                    .exec(new String[] { "osascript", "-e", "output volume of (get volume settings)" });
            String value = IOUtils.toString(p.getInputStream()).trim();
            return Float.valueOf(value) / 100f;
        }

        return 0;
    }

    @Override
    public void setVolume(float volume) throws IOException {
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume value must be in the range [0,1]!");
        }
        Runtime.getRuntime().exec(new String[] { "osascript", "-e", "set volume output volume " + (volume * 100f) });
    }

}
