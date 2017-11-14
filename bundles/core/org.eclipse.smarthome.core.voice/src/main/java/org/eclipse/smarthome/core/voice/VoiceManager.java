/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.core.voice.text.InterpretationException;

/**
 * This service provides functionality around voice services and is the central service to be used directly by others.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public interface VoiceManager {

    /**
     * Speaks the passed string using the default TTS service and default audio sink.
     *
     * @param text The text to say
     */
    void say(String text);

    /**
     * Speaks the passed string using the provided voiceId and the default audio sink.
     * If the voiceId is fully qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     * voiceId is assumed to be available on the default TTS service.
     *
     * @param text The text to say
     * @param voiceId The id of the voice to use (either with or without prefix)
     */
    void say(String text, String voiceId);

    /**
     * Speaks the passed string using the provided voiceId and the given audio sink.
     * If the voiceId is fully qualified (i.e. with a tts prefix), the according TTS service will be used, otherwise the
     * voiceId is assumed to be available on the default TTS service.
     *
     * @param text The text to say
     * @param voiceId The id of the voice to use (either with or without prefix) or null
     * @param sinkId The id of the audio sink to use or null
     */
    void say(String text, String voiceId, String sinkId);

    /**
     * Interprets the passed string using the default services for HLI and locale.
     *
     * @param text The text to interpret
     * @throws InterpretationException
     * @return a human language response
     */
    String interpret(String text) throws InterpretationException;

    /**
     * Interprets the passed string using a particular HLI service and the default locale.
     *
     * @param text The text to interpret
     * @param hliId The id of the HLI service to use
     * @throws InterpretationException
     * @return a human language response
     */
    String interpret(String text, String hliId) throws InterpretationException;

    /**
     * Determines the preferred voice for the currently set locale
     *
     * @param voices a set of voices to chose from
     * @return the preferred voice for the current locale
     */
    Voice getPreferredVoice(Set<Voice> voices);

    /**
     * Starts listening for the keyword that starts a dialog
     *
     * @throws IllegalStateException if required services are not available
     */
    void startDialog();

    /**
     * Starts listening for the keyword that starts a dialog
     *
     * @throws IllegalStateException if required services are not available
     */
    void startDialog(KSService ks, STTService stt, TTSService tts, HumanLanguageInterpreter hli, AudioSource source,
            AudioSink sink, Locale locale, String keyword, String listeningItem);

    /**
     * Retrieves a TTS service.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a TTS service or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    TTSService getTTS();

    /**
     * Retrieves a TTS service with the given id.
     *
     * @param id the id of the TTS service
     * @return a TTS service or null, if no service with this id exists
     */
    TTSService getTTS(String id);

    /**
     * Retrieves all TTS services.
     *
     * @return a collection of TTS services
     */
    Collection<TTSService> getTTSs();

    /**
     * Retrieves a STT service.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a STT service or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    STTService getSTT();

    /**
     * Retrieves a STT service with the given id.
     *
     * @param id the id of the STT service
     * @return a STT service or null, if no service with this id exists
     */
    STTService getSTT(String id);

    /**
     * Retrieves all STT services.
     *
     * @return a collection of STT services
     */
    Collection<STTService> getSTTs();

    /**
     * Retrieves a KS service.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a KS service or null, if no service is available or if a default is configured, but no according service
     *         is found
     */
    KSService getKS();

    /**
     * Retrieves a KS service with the given id.
     *
     * @param id the id of the KS service
     * @return a KS service or null, if no service with this id exists
     */
    KSService getKS(String id);

    /**
     * Retrieves all KS services.
     *
     * @return a collection of KS services
     */
    Collection<KSService> getKSs();

    /**
     * Retrieves a HumanLanguageInterpreter.
     * If a default name is configured and the service available, this is returned. Otherwise, the first available
     * service is returned.
     *
     * @return a HumanLanguageInterpreter or null, if no service is available or if a default is configured, but no
     *         according service is found
     */
    HumanLanguageInterpreter getHLI();

    /**
     * Retrieves a HumanLanguageInterpreter with the given id.
     *
     * @param id the id of the HumanLanguageInterpreter
     * @return a HumanLanguageInterpreter or null, if no interpreter with this id exists
     */
    HumanLanguageInterpreter getHLI(String id);

    /**
     * Retrieves all HumanLanguageInterpreters.
     *
     * @return a collection of HumanLanguageInterpreters
     */
    Collection<HumanLanguageInterpreter> getHLIs();

    /**
     * Returns all available voices in the system from all TTS services.
     *
     * @return a set of available voices
     */
    Set<Voice> getAllVoices();

}