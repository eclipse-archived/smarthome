/**
 * Copyright (c) 2016 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.media.handler;

import java.util.Map;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.handler.ActionHandler;
import org.eclipse.smarthome.automation.handler.BaseModuleHandler;
import org.eclipse.smarthome.core.voice.VoiceManager;

/**
 * This is an ModuleHandler implementation for Actions that trigger a TTS output through "say".
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class SayActionHandler extends BaseModuleHandler<Action> implements ActionHandler {

    public static final String TYPE_ID = "media.SayAction";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_SINK = "sink";

    private final VoiceManager voiceManager;

    public SayActionHandler(Action module, VoiceManager voiceManager) {
        super(module);
        this.voiceManager = voiceManager;
    }

    @Override
    public Map<String, Object> execute(Map<String, Object> context) {
        String text = module.getConfiguration().get(PARAM_TEXT).toString();
        String sink = (String) module.getConfiguration().get(PARAM_SINK);
        voiceManager.say(text, null, sink);
        return null;
    }
}
