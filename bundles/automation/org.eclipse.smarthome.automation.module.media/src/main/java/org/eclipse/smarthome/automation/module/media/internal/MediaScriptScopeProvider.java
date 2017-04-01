/**
 * Copyright (c) 2017 by Deutsche Telekom AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.media.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.automation.module.script.ScriptScopeProvider;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.voice.VoiceManager;

/**
 * This is a scope provider for features that are related to audio and voice support.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
public class MediaScriptScopeProvider implements ScriptScopeProvider {

    private AudioManager audioManager;
    private VoiceManager voiceManager;

    protected void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        this.audioManager = null;
    }

    protected void setVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }

    protected void unsetVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = null;
    }

    @Override
    public Map<String, Object> getScopeElements() {
        Map<String, Object> elements = new HashMap<>();
        elements.put("audio", audioManager);
        elements.put("voice", voiceManager);
        return Collections.unmodifiableMap(elements);
    }

}
