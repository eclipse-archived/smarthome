/**
 * Copyright (c) 2016 Kai Kreuzer and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.automation.module.media.internal;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.automation.Visibility;
import org.eclipse.smarthome.automation.module.media.handler.PlayActionHandler;
import org.eclipse.smarthome.automation.type.ActionType;
import org.eclipse.smarthome.automation.type.Input;
import org.eclipse.smarthome.automation.type.ModuleType;
import org.eclipse.smarthome.automation.type.ModuleTypeProvider;
import org.eclipse.smarthome.automation.type.Output;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameter.Type;
import org.eclipse.smarthome.config.core.ConfigDescriptionParameterBuilder;
import org.eclipse.smarthome.config.core.ParameterOption;
import org.eclipse.smarthome.core.audio.AudioManager;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.common.registry.ProviderChangeListener;

/**
 * This class dynamically provides the Play action type.
 * This is necessary since there is no other way to provide dynamic config param options for module types.
 *
 * @author Kai Kreuzer - Initial contribution and API
 *
 */
public class MediaActionTypeProvider implements ModuleTypeProvider {

    private AudioManager audioManager;

    @SuppressWarnings("unchecked")
    @Override
    public ModuleType getModuleType(String UID, Locale locale) {
        if ("media.PlayAction".equals(UID)) {
            return getPlayActionType(locale);
        } else {
            return null;
        }
    }

    @Override
    public Collection<ModuleType> getModuleTypes(Locale locale) {
        return Collections.singleton(getPlayActionType(locale));
    }

    private ModuleType getPlayActionType(Locale locale) {
        return new ActionType("media.PlayAction", getConfigDesc(locale), "play a sound", "Plays a sound file.", null,
                Visibility.VISIBLE, new ArrayList<Input>(), new ArrayList<Output>());
    }

    private List<ConfigDescriptionParameter> getConfigDesc(Locale locale) {
        ConfigDescriptionParameter param1 = ConfigDescriptionParameterBuilder
                .create(PlayActionHandler.PARAM_SOUND, Type.TEXT).withRequired(true).withLabel("Sound")
                .withDescription("the sound to play").withOptions(getSoundOptions()).withLimitToOptions(true).build();
        ConfigDescriptionParameter param2 = ConfigDescriptionParameterBuilder
                .create(PlayActionHandler.PARAM_SINK, Type.TEXT).withRequired(false).withLabel("Sink")
                .withDescription("the audio sink id").withOptions(getSinkOptions(locale)).withLimitToOptions(true)
                .build();
        List<ConfigDescriptionParameter> params = new ArrayList<>(2);
        params.add(param1);
        params.add(param2);
        return params;
    }

    /**
     * This method creates one option for every file that is found in the sounds directory.
     * As a label, the file extension is removed and the string is capitalized.
     *
     * @return a list of parameter options representing the sound files
     */
    private List<ParameterOption> getSoundOptions() {
        List<ParameterOption> options = new ArrayList<>();
        File soundsDir = Paths.get(ConfigConstants.getConfigFolder(), AudioManager.SOUND_DIR).toFile();
        if (soundsDir.isDirectory()) {
            for (String fileName : soundsDir.list()) {
                if (fileName.contains(".") && !fileName.startsWith(".")) {
                    String soundName = StringUtils.capitalize(fileName.substring(0, fileName.lastIndexOf(".")));
                    options.add(new ParameterOption(fileName, soundName));
                }
            }
        }
        return options;
    }

    /**
     * This method creates one option for every sink that is found in the system.
     *
     * @return a list of parameter options representing the audio sinks
     */
    private List<ParameterOption> getSinkOptions(Locale locale) {
        List<ParameterOption> options = new ArrayList<>();

        for (String sinkId : audioManager.getSinkIds()) {
            AudioSink sink = audioManager.getSink(sinkId);
            options.add(new ParameterOption(sinkId, sink.getLabel(null)));
        }
        return options;
    }

    @Override
    public void addProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    @Override
    public Collection<ModuleType> getAll() {
        return getModuleTypes(null);
    }

    @Override
    public void removeProviderChangeListener(ProviderChangeListener<ModuleType> listener) {
        // does nothing because this provider does not change
    }

    protected void setAudioManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    protected void unsetAudioManager(AudioManager audioManager) {
        this.audioManager = null;
    }
}
