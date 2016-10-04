/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.model.script.actions;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.eclipse.smarthome.model.script.engine.action.ActionDoc;
import org.eclipse.smarthome.model.script.engine.action.ParamDoc;
import org.eclipse.smarthome.model.script.internal.engine.action.VoiceActionService;

/**
 * The static methods of this class are made available as functions in the scripts.
 * This allows a script to use voice features.
 *
 * @author Kai Kreuzer
 */
public class Voice {

    /**
     * Says the given text.
     *
     * @param text the text to speak
     */
    @ActionDoc(text = "says a given text with the default voice")
    public static void say(@ParamDoc(name = "text") Object text) {
        say(text.toString(), null);
    }

    /**
     * Says the given text with a given voice.
     *
     * @param text the text to speak
     * @param voice the name of the voice to use or null, if the default voice should be used. If the voiceId is fully
     *            qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     *            voiceId is assumed to be available on the default TTS service.
     */
    @ActionDoc(text = "says a given text with a given voice")
    public static void say(@ParamDoc(name = "text") String text, @ParamDoc(name = "voice") String voice) {
        say(text, voice, null);
    }

    /**
     * Says the given text with a given voice through the given sink.
     *
     * @param text the text to speak
     * @param voice the name of the voice to use or null, if the default voice should be used. If the voiceId is fully
     *            qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     *            voiceId is assumed to be available on the default TTS service.
     * @param sink the name of audio sink to be used to play the audio or null, if the default sink should
     *            be used
     */
    @ActionDoc(text = "says a given text through the default TTS service with a given voice")
    public static void say(@ParamDoc(name = "text") String text, @ParamDoc(name = "voice") String voice,
            @ParamDoc(name = "sink") String sink) {
        if (StringUtils.isNotBlank(text.toString())) {
            VoiceActionService.voiceManager.say(text, voice, sink);
        }
    }

    /**
     * Interprets the given text.
     *
     * This method uses the default Human Language Interpreter and passes the text to it.
     *
     * @param text the text to interpret
     */
    @ActionDoc(text = "interprets a given text by the default human language interpreter")
    public static void interpret(@ParamDoc(name = "text") Object text) {
        try {
            VoiceActionService.voiceManager.interpret(text.toString());
        } catch (InterpretationException e) {
            say(e.getMessage());
        }
    }

}
