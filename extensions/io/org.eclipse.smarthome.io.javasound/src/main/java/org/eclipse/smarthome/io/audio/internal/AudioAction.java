/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.io.audio.internal;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.io.audio.AudioManager;
import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.eclipse.smarthome.model.script.engine.action.ParamDoc;

public class AudioAction {

    private static AudioManager audioManager;

    @ActionDoc(text = "plays a sound from the sounds folder to the default sink")
    static public void playSound(@ParamDoc(name = "filename", text = "the filename with extension") String filename) {
        audioManager.play(filename);
    }

    @ActionDoc(text = "plays a sound from the sounds folder to the given sink(s)")
    static public void playSound(@ParamDoc(name = "sink", text = "the id of the sink") String sink,
            @ParamDoc(name = "filename", text = "the filename with extension") String filename) {
        audioManager.play(filename, sink);
    }

    @ActionDoc(text = "plays an audio stream from an url to the default sink")
    static public synchronized void playStream(
            @ParamDoc(name = "url", text = "the url of the audio stream") String url) {

        if (url == null) {
            // TODO - mechanism to stop playing a stream
            return;
        }

        audioManager.stream(url);
    }

    @ActionDoc(text = "plays an audio stream from an url t0 the given sink(s)")
    static public synchronized void playStream(@ParamDoc(name = "sink", text = "the id of the sink") String sink,
            @ParamDoc(name = "url", text = "the url of the audio stream") String url) {

        if (url == null || sink == null) {
            // TODO - mechanism to stop playing a stream
            return;
        }

        audioManager.stream(url, sink);
    }

    @ActionDoc(text = "gets the master volume of the host", returns = "volume as a float in the range [0,1]")
    static public float getMasterVolume() throws IOException {
        if (isMacOSX()) {
            return audioManager.getVolume("macosx");
        } else {
            return audioManager.getVolume("javasound");
        }
    }

    @ActionDoc(text = "sets the master volume of the host")
    static public void setMasterVolume(
            @ParamDoc(name = "volume", text = "volume in the range [0,1]") final float volume) throws IOException {
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume value must be in the range [0,1]!");
        }
        if (isMacOSX()) {
            audioManager.setVolume("macosx", volume * 100f);
        } else {
            audioManager.setVolume("javasound", volume);
        }
    }

    @ActionDoc(text = "sets the master volume of the host")
    static public void setMasterVolume(@ParamDoc(name = "percent") final PercentType percent) throws IOException {
        setMasterVolume(percent.toBigDecimal().floatValue() / 100f);
    }

    @ActionDoc(text = "increases the master volume of the host")
    static public void increaseMasterVolume(@ParamDoc(name = "percent") final float percent) throws IOException {
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be in the range (0,100]!");
        }
        Float volume = getMasterVolume();
        if (volume == 0) {
            // as increasing 0 by x percent will still be 0, we have to set some initial positive value
            volume = 0.001f;
        }
        float newVolume = volume * (1f + percent / 100f);
        if (isMacOSX() && newVolume - volume < .01) {
            // the getMasterVolume() only returns integers, so we have to make sure that we
            // increase the volume level at least by 1%.
            newVolume += .01;
        }
        if (newVolume > 1) {
            newVolume = 1;
        }
        setMasterVolume(newVolume);
    }

    @ActionDoc(text = "decreases the master volume of the host")
    static public void decreaseMasterVolume(@ParamDoc(name = "percent") final float percent) throws IOException {
        if (percent <= 0 || percent > 100) {
            throw new IllegalArgumentException("Percent must be in the range (0,100]!");
        }
        float volume = getMasterVolume();
        float newVolume = volume * (1f - percent / 100f);
        if (isMacOSX() && newVolume > 0 && volume - newVolume < .01) {
            // the getMasterVolume() only returns integers, so we have to make sure that we
            // decrease the volume level at least by 1%.
            newVolume -= .01;
        }
        if (newVolume < 0) {
            newVolume = 0;
        }
        setMasterVolume(newVolume);
    }

    private static boolean isMacOSX() {
        return System.getProperty("osgi.os").equals("macosx");
    }

    protected void setAudioManager(AudioManager audioManager) {
        AudioAction.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        AudioAction.audioManager = null;
    }

}
