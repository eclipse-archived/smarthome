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
import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an ModuleHandler implementation for Actions that play a sound file from the file system.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class PlayActionHandler extends BaseModuleHandler<Action> implements ActionHandler {

    public static final String TYPE_ID = "PlayAction";
    public static final String PARAM_SOUND = "sound";

    private final Logger logger = LoggerFactory.getLogger(PlayActionHandler.class);

    private final AudioManager audioManager;

    public PlayActionHandler(Action module, AudioManager audioManager) {
        super(module);
        this.audioManager = audioManager;
    }

    @Override
    public Map<String, Object> execute(Map<String, ?> context) {
        String sound = module.getConfiguration().get(PARAM_SOUND).toString();
        try {
            audioManager.playFile(sound);
        } catch (AudioException e) {
            logger.error("Error playing sound '{}': {}", sound, e.getMessage());
        }
        return null;
    }

}
