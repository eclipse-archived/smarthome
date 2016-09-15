/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an AudioSource from an audio file on the host.
 *
 * @author Karel Goderis - Initial contribution and API
 *
 */
public class FileAudioSource implements AudioSource {

    private static final String RUNTIME_DIR = "runtime";
    private static final String SOUND_DIR = "sounds";
    private static final Logger logger = LoggerFactory.getLogger(FileAudioSource.class);

    private String fileName;

    public FileAudioSource(String fileName) {
        this.fileName = fileName;

        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        bundleContext.registerService(AudioSource.class.getName(), this, new Hashtable<String, Object>());
    }

    @Override
    public String getId() {
        return fileName;
    }

    @Override
    public String getLabel(Locale locale) {
        return "Sound File";
    }

    @Override
    public Set<AudioFormat> getSupportedFormats() {
        HashSet<AudioFormat> audioFormats = new HashSet<AudioFormat>();
        audioFormats
                .add(new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED, null, null, null, null));
        audioFormats.add(new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, null, null, null, null));
        return Collections.unmodifiableSet(audioFormats);
    }

    @Override
    public AudioStream getInputStream() throws AudioException {
        try {
            ;
            InputStream is = new FileInputStream(System.getProperty(ConfigConstants.USERDATA_DIR_PROG_ARGUMENT)
                    + File.separator + SOUND_DIR + File.separator + fileName);
            // InputStream is = new FileInputStream(RUNTIME_DIR + File.separator + SOUND_DIR + File.separator +
            // fileName);
            if (fileName.toLowerCase().endsWith(".wav")) {
                return new FileAudioStream(is, new AudioFormat(AudioFormat.CONTAINER_WAVE, AudioFormat.CODEC_PCM_SIGNED,
                        null, null, null, null));
            } else if (fileName.toLowerCase().endsWith(".mp3")) {
                return new FileAudioStream(is,
                        new AudioFormat(AudioFormat.CODEC_MP3, AudioFormat.CODEC_MP3, null, null, null, null));
            }
        } catch (FileNotFoundException e) {
            logger.error("Cannot find file '{}': {}", new Object[] { fileName, e.getMessage() });
            throw new AudioException("File not found");
        }

        return null;
    }

}
