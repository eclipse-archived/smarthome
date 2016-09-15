/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.voice.VoiceManager;
import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.eclipse.smarthome.model.script.engine.action.ParamDoc;

public class VoiceAction {

    private static VoiceManager voiceManager;

    /**
     * Says the given text..
     *
     *
     * @param text the text to speak
     */
    @ActionDoc(text = "says a given text with the default voice")
    static public void say(@ParamDoc(name = "text") Object text) {
        say(text.toString(), null);
    }

    /**
     * Text-to-speech with a given voice.
     *
     *
     * @param text the text to speak
     * @param voice the name of the voice to use or null, if the default voice should be used. If the voiceId is fully
     *            qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     *            voiceId is assumed to be available on the default TTS service.
     */
    @ActionDoc(text = "says a given text through the default TTS service with a given voice")
    static public void say(@ParamDoc(name = "text") String text, @ParamDoc(name = "voice") String voice) {
        say(text, voice, null);
    }

    /**
     * Text-to-speech with a given voice through the given sink
     *
     *
     * @param text the text to speak
     * @param voice the name of the voice to use or null, if the default voice should be used. If the voiceId is fully
     *            qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     *            voiceId is assumed to be available on the default TTS service.
     * @param sink the name of audio sink to be used to play the audio or null, if the default sink should
     *            be used
     */
    @ActionDoc(text = "says a given text through the default TTS service with a given voice")
    static public void say(@ParamDoc(name = "text") String text, @ParamDoc(name = "voice") String voice,
            @ParamDoc(name = "sink") String sink) {
        if (StringUtils.isNotBlank(text.toString())) {
            voiceManager.say(text, voice, sink);
        }
    }

    protected void setVoiceManager(VoiceManager voiceManager) {
        VoiceAction.voiceManager = voiceManager;
    }

    protected void unsetVoiceManager(VoiceManager voiceManager) {
        VoiceAction.voiceManager = null;
    }

}
