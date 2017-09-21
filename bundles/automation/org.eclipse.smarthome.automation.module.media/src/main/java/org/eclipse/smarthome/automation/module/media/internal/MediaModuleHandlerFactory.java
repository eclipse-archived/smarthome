/**
 * Copyright (c) 2016 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.media.internal;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.smarthome.automation.Action;
import org.eclipse.smarthome.automation.Module;
import org.eclipse.smarthome.automation.handler.BaseModuleHandlerFactory;
import org.eclipse.smarthome.automation.handler.ModuleHandler;
import org.eclipse.smarthome.automation.handler.ModuleHandlerFactory;
import org.eclipse.smarthome.automation.module.media.handler.PlayActionHandler;
import org.eclipse.smarthome.automation.module.media.handler.SayActionHandler;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.voice.VoiceManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.google.common.collect.ImmutableList;

/**
 *
 * @author Kai Kreuzer - Initial contribution
 */
@Component(service = ModuleHandlerFactory.class)
public class MediaModuleHandlerFactory extends BaseModuleHandlerFactory {

    private static final Collection<String> types = ImmutableList.of(SayActionHandler.TYPE_ID,
            PlayActionHandler.TYPE_ID);
    private VoiceManager voiceManager;
    private AudioManager audioManager;

    @Override
    public Collection<String> getTypes() {
        return new ArrayList<>(types);
    }

    @Override
    protected ModuleHandler internalCreate(Module module, String ruleUID) {
        if (module instanceof Action) {
            switch (module.getTypeUID()) {
                case SayActionHandler.TYPE_ID:
                    return new SayActionHandler((Action) module, voiceManager);
                case PlayActionHandler.TYPE_ID:
                    return new PlayActionHandler((Action) module, audioManager);
                default:
                    break;
            }
        }
        return null;
    }

    @Reference
    protected void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        this.audioManager = null;
    }

    @Reference
    protected void setVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = voiceManager;
    }

    protected void unsetVoiceManager(VoiceManager voiceManager) {
        this.voiceManager = null;
    }
}
