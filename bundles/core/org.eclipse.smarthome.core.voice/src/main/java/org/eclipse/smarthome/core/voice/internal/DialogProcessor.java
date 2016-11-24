/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.smarthome.core.voice.internal;

import java.util.HashSet;
import java.util.Locale;

import org.eclipse.smarthome.core.audio.AudioException;
import org.eclipse.smarthome.core.audio.AudioFormat;
import org.eclipse.smarthome.core.audio.AudioSink;
import org.eclipse.smarthome.core.audio.AudioSource;
import org.eclipse.smarthome.core.audio.AudioStream;
import org.eclipse.smarthome.core.audio.UnsupportedAudioFormatException;
import org.eclipse.smarthome.core.voice.KSErrorEvent;
import org.eclipse.smarthome.core.voice.KSEvent;
import org.eclipse.smarthome.core.voice.KSException;
import org.eclipse.smarthome.core.voice.KSListener;
import org.eclipse.smarthome.core.voice.KSService;
import org.eclipse.smarthome.core.voice.KSpottedEvent;
import org.eclipse.smarthome.core.voice.RecognitionStopEvent;
import org.eclipse.smarthome.core.voice.STTEvent;
import org.eclipse.smarthome.core.voice.STTException;
import org.eclipse.smarthome.core.voice.STTListener;
import org.eclipse.smarthome.core.voice.STTService;
import org.eclipse.smarthome.core.voice.STTServiceHandle;
import org.eclipse.smarthome.core.voice.SpeechRecognitionErrorEvent;
import org.eclipse.smarthome.core.voice.SpeechRecognitionEvent;
import org.eclipse.smarthome.core.voice.TTSException;
import org.eclipse.smarthome.core.voice.TTSService;
import org.eclipse.smarthome.core.voice.Voice;
import org.eclipse.smarthome.core.voice.text.HumanLanguageInterpreter;
import org.eclipse.smarthome.core.voice.text.InterpretationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An instance of this class can handle a complete dialog with the user. It orchestrates the keyword spotting, the stt
 * and tts services together with the human language interpreter.
 *
 * @author Kai Kreuzer - Initial contribution and API
 */
public class DialogProcessor implements KSListener, STTListener {

    private final Logger logger = LoggerFactory.getLogger(DialogProcessor.class);

    /**
     * If the processor should spot new keywords
     */
    private boolean processing = true;

    /**
     * If the STT server is in the process of aborting
     */
    private boolean isSTTServerAborting = false;

    private STTServiceHandle sttServiceHandle;

    private final KSService ks;
    private final STTService stt;
    private final TTSService tts;
    private final HumanLanguageInterpreter hli;
    private final AudioSource source;
    private final AudioSink sink;
    private final Locale locale;
    private final String keyword;

    private final AudioFormat format;

    public DialogProcessor(KSService ks, STTService stt, TTSService tts, HumanLanguageInterpreter hli,
            AudioSource source, AudioSink sink, Locale locale, String keyword) {
        this.locale = locale;
        this.ks = ks;
        this.hli = hli;
        this.stt = stt;
        this.tts = tts;
        this.source = source;
        this.sink = sink;
        this.keyword = keyword;
        this.format = AudioFormat.getBestMatch(source.getSupportedFormats(), sink.getSupportedFormats());
    }

    public void start() {
        try {
            ks.spot(this, source.getInputStream(format), locale, this.keyword);
        } catch (KSException | AudioException e) {
            logger.error("Encountered error calling spot: {}", e.getMessage());
        }
    }

    @Override
    public void ksEventReceived(KSEvent ksEvent) {
        if (!processing) {
            processing = true;
            this.isSTTServerAborting = false;
            if (ksEvent instanceof KSpottedEvent) {
                if (stt != null) {
                    try {
                        this.sttServiceHandle = stt.recognize(this, source.getInputStream(format), this.locale,
                                new HashSet<String>());
                    } catch (STTException | AudioException e) {
                        say("Error during recognition: " + e.getMessage());
                    }
                }
            } else if (ksEvent instanceof KSErrorEvent) {
                KSErrorEvent kse = (KSErrorEvent) ksEvent;
                say("Encountered error spotting keywords, " + kse.getMessage());
            }
        }
    }

    @Override
    public synchronized void sttEventReceived(STTEvent sttEvent) {
        if (sttEvent instanceof SpeechRecognitionEvent) {
            if (false == this.isSTTServerAborting) {
                this.sttServiceHandle.abort();
                this.isSTTServerAborting = true;
                SpeechRecognitionEvent sre = (SpeechRecognitionEvent) sttEvent;
                String question = sre.getTranscript();
                try {
                    this.processing = false;
                    say(hli.interpret(this.locale, question));
                } catch (InterpretationException e) {
                    say(e.getMessage());
                }
            }
        } else if (sttEvent instanceof RecognitionStopEvent) {
            this.processing = false;
        } else if (sttEvent instanceof SpeechRecognitionErrorEvent) {
            if (false == this.isSTTServerAborting) {
                this.sttServiceHandle.abort();
                this.isSTTServerAborting = true;
                this.processing = false;
                SpeechRecognitionErrorEvent sre = (SpeechRecognitionErrorEvent) sttEvent;
                say("Encountered error: " + sre.getMessage());
            }
        }
    }

    /**
     * Says the passed command
     *
     * @param text The text to say
     */
    protected void say(String text) {
        try {
            Voice voice = null;
            for (Voice currentVoice : tts.getAvailableVoices()) {
                if (this.locale.getLanguage().equals(currentVoice.getLocale().getLanguage())) {
                    voice = currentVoice;
                    break;
                }
            }
            if (null == voice) {
                throw new TTSException("Unable to find a suitable voice");
            }
            AudioStream audioStream = tts.synthesize(text, voice, null);
            sink.process(audioStream);
        } catch (TTSException | UnsupportedAudioFormatException e) {
            logger.error("Error saying '{}'", text);
        }
    }

}
