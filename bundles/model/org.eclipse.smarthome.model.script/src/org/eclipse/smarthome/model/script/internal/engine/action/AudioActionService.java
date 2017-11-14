/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.engine.action;

import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.model.script.actions.Audio;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

public class AudioActionService implements ActionService {

    public static AudioManager audioManager;

    @Override
    public String getActionClassName() {
        return Audio.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return Audio.class;
    }

    protected void setAudioManager(AudioManager audioManager) {
        AudioActionService.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        AudioActionService.audioManager = null;
    }

}
