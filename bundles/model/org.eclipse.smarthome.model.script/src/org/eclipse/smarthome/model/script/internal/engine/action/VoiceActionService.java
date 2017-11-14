/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.internal.engine.action;

import org.eclipse.smarthome.core.voice.VoiceManager;
import org.eclipse.smarthome.model.script.actions.Voice;
import org.eclipse.smarthome.model.script.engine.action.ActionService;

/**
 * This class registers an OSGi service for the voice action.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class VoiceActionService implements ActionService {

    public static VoiceManager voiceManager;

    @Override
    public String getActionClassName() {
        return Voice.class.getCanonicalName();
    }

    @Override
    public Class<?> getActionClass() {
        return Voice.class;
    }

    protected void setVoiceManager(VoiceManager voiceManager) {
        VoiceActionService.voiceManager = voiceManager;
    }

    protected void unsetVoiceManager(VoiceManager voiceManager) {
        VoiceActionService.voiceManager = null;
    }

}
